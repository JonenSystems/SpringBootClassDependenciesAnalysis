package com.example.springbootprojectanalyser.controller;

import com.example.springbootprojectanalyser.model.dto.ClassDiagramDto;
import com.example.springbootprojectanalyser.model.dto.EndpointDto;
import com.example.springbootprojectanalyser.model.form.ClassDiagramForm;
import com.example.springbootprojectanalyser.repository.EndpointRepository;
import com.example.springbootprojectanalyser.repository.ProjectRepository;
import com.example.springbootprojectanalyser.service.ClassDiagramService;
import com.example.springbootprojectanalyser.service.EndpointExtractionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * クラス図作成コントローラー
 */
@Controller
public class ClassDiagramController {

    private final EndpointExtractionService endpointExtractionService;
    private final ClassDiagramService classDiagramService;
    private final ProjectRepository projectRepository;
    private final EndpointRepository endpointRepository;

    public ClassDiagramController(
            EndpointExtractionService endpointExtractionService,
            ClassDiagramService classDiagramService,
            ProjectRepository projectRepository,
            EndpointRepository endpointRepository) {
        this.endpointExtractionService = endpointExtractionService;
        this.classDiagramService = classDiagramService;
        this.projectRepository = projectRepository;
        this.endpointRepository = endpointRepository;
    }

    @GetMapping({"/classdiagram", "/classdiagram/"})
    public String index(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", ClassDiagramForm.empty());
        }
        // プロジェクト一覧を取得してモデルに追加
        model.addAttribute("projects", projectRepository.findAll());
        // フォームから選択済みエンドポイントIDを取得してモデルに追加（エンドポイント選択の保持のため）
        // リダイレクト属性のselectedEndpointIdが優先されるが、フォームにも設定する
        if (model.containsAttribute("form")) {
            ClassDiagramForm form = (ClassDiagramForm) model.getAttribute("form");
            if (form != null && form.selectedEndpointId() != null && !form.selectedEndpointId().isEmpty()) {
                // リダイレクト属性にselectedEndpointIdがない場合のみ、フォームから設定
                if (!model.containsAttribute("selectedEndpointId")) {
                    model.addAttribute("selectedEndpointId", form.selectedEndpointId());
                }
            }
        }
        return "classdiagram/index";
    }

    @PostMapping("/classdiagram/extract")
    public String extractEndpoints(
            @Valid @ModelAttribute("form") ClassDiagramForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/classdiagram/";
        }

        try {
            // エンドポイント情報を抽出
            String targetPackagePattern = "**"; // 全パッケージを対象
            List<EndpointDto> endpoints = endpointExtractionService.extractEndpoints(
                form.targetProjectPath(),
                targetPackagePattern
            );
            
            // プロジェクトIDを取得
            Long projectId = projectRepository.findByRootPath(form.targetProjectPath())
                .map(p -> p.getId())
                .orElseThrow(() -> new IllegalArgumentException("プロジェクトが見つかりません"));
            
            redirectAttributes.addFlashAttribute("endpoints", endpoints);
            redirectAttributes.addFlashAttribute("projectId", projectId);
            redirectAttributes.addFlashAttribute("form", form);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "エラー: " + e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/classdiagram/";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "解析エラー: " + e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/classdiagram/";
        }

        return "redirect:/classdiagram/";
    }

    @PostMapping("/classdiagram/generate")
    public String generateClassDiagram(
            @RequestParam("selectedEndpointId") String selectedEndpointId,
            @RequestParam("projectId") Long projectId,
            RedirectAttributes redirectAttributes) {
        
        try {
            UUID endpointId = UUID.fromString(selectedEndpointId);
            
            // 選択されたエンドポイントの情報を取得（URI、HTTPメソッド、クラス名を保存するため）
            com.example.springbootprojectanalyser.model.entity.Endpoint selectedEndpoint = 
                endpointRepository.findById(selectedEndpointId)
                    .orElseThrow(() -> new IllegalArgumentException("エンドポイントが見つかりません: " + selectedEndpointId));
            
            String selectedUri = selectedEndpoint.getUri();
            String selectedHttpMethod = selectedEndpoint.getHttpMethod().getMethodName();
            String selectedClassName = selectedEndpoint.getClassEntity().getSimpleName();
            
            ClassDiagramDto classDiagram = classDiagramService.generateClassDiagram(endpointId, projectId);
            
            // プロジェクト情報を取得
            com.example.springbootprojectanalyser.model.entity.Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("プロジェクトが見つかりません"));
            
            // エンドポイント情報を再取得（クラス図表示後もエンドポイント選択を可能にするため）
            String targetPackagePattern = "**"; // 全パッケージを対象
            List<EndpointDto> endpoints = endpointExtractionService.extractEndpoints(
                project.getRootPath(),
                targetPackagePattern
            );
            
            // 再取得後のエンドポイント一覧から、URI、HTTPメソッド、クラス名で一致するエンドポイントを探す
            String newSelectedEndpointId = endpoints.stream()
                .filter(e -> e.uri().equals(selectedUri) && 
                            e.httpMethodName().equals(selectedHttpMethod) && 
                            e.className().equals(selectedClassName))
                .map(EndpointDto::endpointId)
                .findFirst()
                .orElse(null);
            
            // フォーム情報を作成（プロジェクトパスを保持）
            ClassDiagramForm form = new ClassDiagramForm(project.getRootPath(), 
                newSelectedEndpointId != null ? newSelectedEndpointId : selectedEndpointId);
            
            redirectAttributes.addFlashAttribute("classDiagram", classDiagram);
            redirectAttributes.addFlashAttribute("selectedEndpointId", 
                newSelectedEndpointId != null ? newSelectedEndpointId : selectedEndpointId);
            redirectAttributes.addFlashAttribute("selectedEndpointUri", selectedUri);
            redirectAttributes.addFlashAttribute("selectedEndpointHttpMethod", selectedHttpMethod);
            redirectAttributes.addFlashAttribute("selectedEndpointClassName", selectedClassName);
            redirectAttributes.addFlashAttribute("projectId", projectId);
            redirectAttributes.addFlashAttribute("endpoints", endpoints);
            redirectAttributes.addFlashAttribute("form", form);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "エラー: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "クラス図生成エラー: " + e.getMessage());
        }

        return "redirect:/classdiagram/";
    }

    @GetMapping("/classdiagram/download-files")
    public org.springframework.http.ResponseEntity<String> downloadConcatenatedFiles(
            @RequestParam("projectId") Long projectId,
            @RequestParam("endpointUri") String endpointUri,
            @RequestParam("httpMethod") String httpMethod) {
        
        try {
            // プロジェクト情報を取得
            com.example.springbootprojectanalyser.model.entity.Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("プロジェクトが見つかりません"));
            
            // クラス図を生成してファイルパス情報を取得
            // エンドポイントを検索
            String targetPackagePattern = "**";
            List<EndpointDto> endpoints = endpointExtractionService.extractEndpoints(
                project.getRootPath(),
                targetPackagePattern
            );
            
            EndpointDto targetEndpoint = endpoints.stream()
                .filter(e -> e.uri().equals(endpointUri) && e.httpMethodName().equals(httpMethod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("エンドポイントが見つかりません"));
            
            UUID endpointId = UUID.fromString(targetEndpoint.endpointId());
            ClassDiagramDto classDiagram = classDiagramService.generateClassDiagram(endpointId, projectId);
            
            // ファイル内容を連結
            String concatenatedContent = concatenateFiles(project.getRootPath(), classDiagram.classFilePaths());
            
            // ファイル名を生成（エンドポイント情報を含む）
            String fileName = generateFileName(endpointUri, httpMethod);
            
            // レスポンスヘッダーを設定
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", fileName);
            
            return org.springframework.http.ResponseEntity.ok()
                .headers(headers)
                .body(concatenatedContent);
                
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body("エラー: " + e.getMessage());
        }
    }

    /**
     * ファイル内容を連結する
     */
    private String concatenateFiles(String projectRootPath, Map<String, String> classFilePaths) {
        StringBuilder sb = new StringBuilder();
        java.nio.file.Path projectRoot = java.nio.file.Paths.get(projectRootPath);
        
        // プロジェクトルートからJavaファイルを収集（キャッシュ）
        Map<String, java.nio.file.Path> javaFileCache = new HashMap<>();
        try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(projectRoot)) {
            paths.filter(java.nio.file.Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.toString().contains("target"))
                .filter(p -> !p.toString().contains(".git"))
                .forEach(javaFile -> {
                    try {
                        String relativePath = projectRoot.relativize(javaFile).toString().replace('\\', '/');
                        javaFileCache.put(relativePath, javaFile);
                    } catch (Exception e) {
                        // 相対パス取得エラーは無視
                    }
                });
        } catch (Exception e) {
            // ファイル検索エラーは無視
        }
        
        for (Map.Entry<String, String> entry : classFilePaths.entrySet()) {
            String fqn = entry.getKey();
            String filePath = entry.getValue();
            
            // まず、生成されたファイルパスで検索
            java.nio.file.Path fullPath = projectRoot.resolve(filePath);
            java.nio.file.Path foundFile = null;
            String foundPath = filePath;
            
            if (java.nio.file.Files.exists(fullPath) && java.nio.file.Files.isRegularFile(fullPath)) {
                foundFile = fullPath;
            } else {
                // ファイルが見つからない場合、クラスFQNから複数の可能性のあるパスを試す
                String[] possiblePaths = generatePossiblePaths(fqn);
                for (String possiblePath : possiblePaths) {
                    java.nio.file.Path possibleFullPath = projectRoot.resolve(possiblePath);
                    if (java.nio.file.Files.exists(possibleFullPath) && java.nio.file.Files.isRegularFile(possibleFullPath)) {
                        foundFile = possibleFullPath;
                        foundPath = possiblePath;
                        break;
                    }
                }
                
                // まだ見つからない場合、キャッシュからクラス名で検索
                if (foundFile == null) {
                    int lastDotIndex = fqn.lastIndexOf('.');
                    String className = lastDotIndex >= 0 ? fqn.substring(lastDotIndex + 1) : fqn;
                    String searchFileName = className + ".java";
                    
                    for (Map.Entry<String, java.nio.file.Path> cacheEntry : javaFileCache.entrySet()) {
                        if (cacheEntry.getKey().endsWith("/" + searchFileName) || cacheEntry.getKey().equals(searchFileName)) {
                            // ファイル内容を確認してFQNが一致するか検証
                            try {
                                JavaParser parser = new JavaParser();
                                CompilationUnit cu = parser.parse(cacheEntry.getValue()).getResult().orElse(null);
                                if (cu != null) {
                                    String filePackageName = cu.getPackageDeclaration()
                                        .map(pd -> pd.getNameAsString())
                                        .orElse("");
                                    List<ClassOrInterfaceDeclaration> classDecls = 
                                        cu.findAll(ClassOrInterfaceDeclaration.class);
                                    for (ClassOrInterfaceDeclaration classDecl : classDecls) {
                                        String fileClassName = classDecl.getNameAsString();
                                        String fileFqn = filePackageName.isEmpty() 
                                            ? fileClassName 
                                            : filePackageName + "." + fileClassName;
                                        if (fileFqn.equals(fqn)) {
                                            foundFile = cacheEntry.getValue();
                                            foundPath = cacheEntry.getKey();
                                            break;
                                        }
                                    }
                                    if (foundFile != null) {
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                // パースエラーは無視
                            }
                        }
                    }
                }
            }
            
            // ファイルヘッダーを追加
            sb.append("===== FILE: ").append(foundPath).append(" =====\n");
            
            if (foundFile != null && java.nio.file.Files.exists(foundFile) && java.nio.file.Files.isRegularFile(foundFile)) {
                try {
                    // ファイル内容を読み込む
                    String content = java.nio.file.Files.readString(foundFile, java.nio.charset.StandardCharsets.UTF_8);
                    sb.append(content);
                    
                    // ファイル間に空行を追加
                    if (!content.endsWith("\n")) {
                        sb.append("\n");
                    }
                    sb.append("\n");
                } catch (Exception e) {
                    // ファイル読み込みエラー
                    sb.append("// Error reading file: ").append(e.getMessage()).append("\n\n");
                }
            } else {
                // ファイルが存在しない場合
                sb.append("// File not found\n\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * クラスのFQNから可能なファイルパスを生成する
     */
    private String[] generatePossiblePaths(String fqn) {
        int lastDotIndex = fqn.lastIndexOf('.');
        String packageName = lastDotIndex >= 0 ? fqn.substring(0, lastDotIndex) : "";
        String className = lastDotIndex >= 0 ? fqn.substring(lastDotIndex + 1) : fqn;
        String packagePath = packageName.replace('.', '/');
        
        List<String> paths = new ArrayList<>();
        
        // src/main/java と src/test/java の両方を試す
        if (packagePath.isEmpty()) {
            paths.add("src/main/java/" + className + ".java");
            paths.add("src/test/java/" + className + ".java");
        } else {
            paths.add("src/main/java/" + packagePath + "/" + className + ".java");
            paths.add("src/test/java/" + packagePath + "/" + className + ".java");
        }
        
        return paths.toArray(new String[0]);
    }

    /**
     * ファイル名を生成する（エンドポイント情報を含む）
     */
    private String generateFileName(String endpointUri, String httpMethod) {
        // URIからファイル名に使用できない文字を置換
        String safeUri = endpointUri.replace("/", "_")
                                   .replace("\\", "_")
                                   .replace(":", "_")
                                   .replace("*", "_")
                                   .replace("?", "_")
                                   .replace("\"", "_")
                                   .replace("<", "_")
                                   .replace(">", "_")
                                   .replace("|", "_");
        
        // 空文字列の場合は"root"に置換
        if (safeUri.isEmpty() || safeUri.equals("_")) {
            safeUri = "root";
        }
        
        // 先頭のアンダースコアを削除
        safeUri = safeUri.replaceAll("^_+", "");
        
        String fileName = safeUri + "(" + httpMethod + ").txt";
        return fileName;
    }
}

