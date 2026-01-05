package com.example.springbootprojectanalyser.service.impl;

import com.example.springbootprojectanalyser.model.dto.EndpointDto;
import com.example.springbootprojectanalyser.model.entity.*;
import com.example.springbootprojectanalyser.repository.*;
import com.example.springbootprojectanalyser.service.EndpointExtractionService;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * エンドポイント抽出サービス実装クラス
 */
@Service
public class EndpointExtractionServiceImpl implements EndpointExtractionService {

    private final ProjectRepository projectRepository;
    private final ClassEntityRepository classEntityRepository;
    private final EndpointRepository endpointRepository;
    private final HttpMethodRepository httpMethodRepository;

    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of(
        "Controller", "RestController", "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController"
    );

    private static final Set<String> MAPPING_ANNOTATIONS = Set.of(
        "GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping", "RequestMapping",
        "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.PostMapping",
        "org.springframework.web.bind.annotation.PutMapping",
        "org.springframework.web.bind.annotation.DeleteMapping",
        "org.springframework.web.bind.annotation.PatchMapping",
        "org.springframework.web.bind.annotation.RequestMapping"
    );

    public EndpointExtractionServiceImpl(
            ProjectRepository projectRepository,
            ClassEntityRepository classEntityRepository,
            EndpointRepository endpointRepository,
            HttpMethodRepository httpMethodRepository) {
        this.projectRepository = projectRepository;
        this.classEntityRepository = classEntityRepository;
        this.endpointRepository = endpointRepository;
        this.httpMethodRepository = httpMethodRepository;
    }

    @Override
    @Transactional
    public List<EndpointDto> extractEndpoints(String targetProjectPath, String targetPackagePattern) {
        Path projectRoot = Paths.get(targetProjectPath);
        if (!Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
            throw new IllegalArgumentException("指定されたパスが存在しないか、ディレクトリではありません: " + targetProjectPath);
        }

        // プロジェクトを取得または作成
        Project project = projectRepository.findByRootPath(targetProjectPath)
            .orElseGet(() -> {
                Project newProject = new Project(targetProjectPath);
                return projectRepository.save(newProject);
            });

        // 既存のエンドポイントを削除
        endpointRepository.deleteAll();

        // Javaファイルを収集
        List<Path> javaFiles = collectJavaFiles(projectRoot);
        
        JavaParser parser = new JavaParser();
        List<EndpointDto> endpoints = new ArrayList<>();

        for (Path javaFile : javaFiles) {
            try {
                CompilationUnit cu = parser.parse(javaFile).getResult().orElse(null);
                if (cu == null) {
                    continue;
                }

                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                    if (isController(classDecl)) {
                        String classFqn = getFullQualifiedName(cu, classDecl);
                        ClassEntity classEntity = classEntityRepository
                            .findByProjectAndFullQualifiedName(project, classFqn)
                            .orElse(null);
                        
                        if (classEntity == null) {
                            return;
                        }

                        // クラスレベルのRequestMappingを取得
                        String basePath = getBasePath(classDecl);

                        // メソッドレベルのマッピングを処理
                        classDecl.getMethods().forEach(method -> {
                            extractEndpointFromMethod(method, classEntity, basePath, endpoints);
                        });
                    }
                });
            } catch (Exception e) {
                System.err.println("Error parsing file: " + javaFile + " - " + e.getMessage());
            }
        }

        return endpoints;
    }

    private List<Path> collectJavaFiles(Path root) {
        try {
            return Files.walk(root)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.toString().contains("target"))
                .filter(p -> !p.toString().contains(".git"))
                .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private boolean isController(ClassOrInterfaceDeclaration classDecl) {
        return classDecl.getAnnotations().stream()
            .anyMatch(ann -> {
                String name = ann.getNameAsString();
                return CONTROLLER_ANNOTATIONS.contains(name) || 
                       name.endsWith("Controller") || 
                       name.endsWith("RestController");
            });
    }

    private String getFullQualifiedName(CompilationUnit cu, ClassOrInterfaceDeclaration classDecl) {
        String packageName = cu.getPackageDeclaration()
            .map(p -> p.getNameAsString())
            .orElse("");
        String className = classDecl.getNameAsString();
        return packageName.isEmpty() ? className : packageName + "." + className;
    }

    private String getBasePath(ClassOrInterfaceDeclaration classDecl) {
        return classDecl.getAnnotations().stream()
            .filter(ann -> ann.getNameAsString().equals("RequestMapping") || 
                          ann.getNameAsString().endsWith("RequestMapping"))
            .findFirst()
            .map(this::extractPathFromAnnotation)
            .orElse("");
    }

    private void extractEndpointFromMethod(MethodDeclaration method, ClassEntity classEntity, 
                                         String basePath, List<EndpointDto> endpoints) {
        for (AnnotationExpr ann : method.getAnnotations()) {
            String annName = ann.getNameAsString();
            if (MAPPING_ANNOTATIONS.contains(annName) || annName.endsWith("Mapping")) {
                String httpMethod = determineHttpMethod(annName);
                String path = extractPathFromAnnotation(ann);
                
                String fullPath = combinePaths(basePath, path);
                
                HttpMethod httpMethodEntity = httpMethodRepository.findByMethodName(httpMethod)
                    .orElseGet(() -> {
                        HttpMethod newMethod = new HttpMethod(httpMethod);
                        return httpMethodRepository.save(newMethod);
                    });

                Endpoint endpoint = new Endpoint(classEntity, fullPath, httpMethodEntity);
                endpoint = endpointRepository.save(endpoint);

                endpoints.add(new EndpointDto(
                    endpoint.getEndpointId(),
                    classEntity.getId(),
                    classEntity.getSimpleName(),
                    endpoint.getUri(),
                    httpMethodEntity.getId(),
                    httpMethodEntity.getMethodName()
                ));
            }
        }
    }

    private String determineHttpMethod(String annotationName) {
        if (annotationName.contains("GetMapping") || annotationName.endsWith("GetMapping")) {
            return "GET";
        } else if (annotationName.contains("PostMapping") || annotationName.endsWith("PostMapping")) {
            return "POST";
        } else if (annotationName.contains("PutMapping") || annotationName.endsWith("PutMapping")) {
            return "PUT";
        } else if (annotationName.contains("DeleteMapping") || annotationName.endsWith("DeleteMapping")) {
            return "DELETE";
        } else if (annotationName.contains("PatchMapping") || annotationName.endsWith("PatchMapping")) {
            return "PATCH";
        } else {
            return "GET"; // RequestMappingのデフォルト
        }
    }

    private String extractPathFromAnnotation(AnnotationExpr ann) {
        if (ann instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr singleAnn = (SingleMemberAnnotationExpr) ann;
            if (singleAnn.getMemberValue() instanceof StringLiteralExpr) {
                return ((StringLiteralExpr) singleAnn.getMemberValue()).getValue();
            }
        } else if (ann instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normalAnn = (NormalAnnotationExpr) ann;
            Optional<MemberValuePair> valuePair = normalAnn.getPairs().stream()
                .filter(pair -> pair.getNameAsString().equals("value") || 
                               pair.getNameAsString().equals("path"))
                .findFirst();
            
            if (valuePair.isPresent() && valuePair.get().getValue() instanceof StringLiteralExpr) {
                return ((StringLiteralExpr) valuePair.get().getValue()).getValue();
            }
        }
        return "";
    }

    private String combinePaths(String basePath, String path) {
        if (basePath.isEmpty()) {
            return path.isEmpty() ? "/" : path;
        }
        if (path.isEmpty()) {
            return basePath;
        }
        String combined = basePath + path;
        return combined.replaceAll("/+", "/");
    }
}

