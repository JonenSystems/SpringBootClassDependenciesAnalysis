package com.example.springbootprojectanalyser.service.impl;

import com.example.springbootprojectanalyser.model.dto.AnalysisRequestDto;
import com.example.springbootprojectanalyser.model.dto.AnalysisResultDto;
import com.example.springbootprojectanalyser.model.dto.PackageSummaryDto;
import com.example.springbootprojectanalyser.model.entity.Class;
import com.example.springbootprojectanalyser.model.entity.ClassDependency;
import com.example.springbootprojectanalyser.model.entity.Package;
import com.example.springbootprojectanalyser.model.entity.Project;
import com.example.springbootprojectanalyser.repository.ClassDependencyRepository;
import com.example.springbootprojectanalyser.repository.ClassRepository;
import com.example.springbootprojectanalyser.repository.PackageRepository;
import com.example.springbootprojectanalyser.repository.ProjectRepository;
import com.example.springbootprojectanalyser.service.AnalysisService;
import com.example.springbootprojectanalyser.service.analyzer.BasicTypeDependencyAnalyzer;
import com.example.springbootprojectanalyser.util.JavaParserUtil;
import com.example.springbootprojectanalyser.util.JavaSourceFileScanner;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 解析サービス実装クラス
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final ProjectRepository projectRepository;
    private final PackageRepository packageRepository;
    private final ClassRepository classRepository;
    private final ClassDependencyRepository classDependencyRepository;
    private final BasicTypeDependencyAnalyzer basicTypeDependencyAnalyzer;

    public AnalysisServiceImpl(
        ProjectRepository projectRepository,
        PackageRepository packageRepository,
        ClassRepository classRepository,
        ClassDependencyRepository classDependencyRepository,
        BasicTypeDependencyAnalyzer basicTypeDependencyAnalyzer
    ) {
        this.projectRepository = projectRepository;
        this.packageRepository = packageRepository;
        this.classRepository = classRepository;
        this.classDependencyRepository = classDependencyRepository;
        this.basicTypeDependencyAnalyzer = basicTypeDependencyAnalyzer;
    }

    @Override
    @Transactional
    public AnalysisResultDto executeAnalysis(AnalysisRequestDto request) {
        String targetProjectPath = request.targetProjectPath();
        String targetPackagePattern = request.targetPackagePattern();

        // プロジェクトの取得または作成
        Project project = projectRepository.findByRootPath(targetProjectPath)
            .orElseGet(() -> {
                String projectName = Path.of(targetProjectPath).getFileName().toString();
                return projectRepository.save(new Project(projectName, targetProjectPath));
            });

        // 解析バッチIDの生成
        String analysisBatchId = UUID.randomUUID().toString();

        // Javaファイルの走査
        List<Path> javaFiles;
        try {
            javaFiles = JavaSourceFileScanner.scanJavaFiles(targetProjectPath, targetPackagePattern);
        } catch (IOException e) {
            throw new RuntimeException("ファイル走査中にエラーが発生しました: " + e.getMessage(), e);
        }

        // パッケージごとのクラス情報を収集（重複除去のためSetを使用）
        Map<String, java.util.Set<Class>> packageClassSetMap = new HashMap<>();
        Map<String, Package> packageMap = new HashMap<>();

        for (Path javaFile : javaFiles) {
            JavaParserUtil.parse(javaFile).ifPresent(cu -> {
                cu.getPackageDeclaration().ifPresent(pkgDecl -> {
                    String packageName = pkgDecl.getNameAsString();
                    
                    // パッケージの取得または作成
                    Package packageEntity = packageMap.computeIfAbsent(packageName, name -> {
                        return packageRepository.findByFullNameAndProject(name, project)
                            .orElseGet(() -> {
                                Package newPackage = new Package(project, name, null, name);
                                return packageRepository.save(newPackage);
                            });
                    });

                    // クラス情報の抽出（トップレベルのクラスのみ）
                    cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                        // トップレベルのクラスのみを対象とする（内部クラスは除外）
                        if (classDecl.isNestedType()) {
                            return;
                        }
                        
                        String className = classDecl.getNameAsString();
                        String fullQualifiedName = packageName + "." + className;
                        
                        // クラスの取得または作成
                        Class classEntity = classRepository.findByFullQualifiedName(fullQualifiedName)
                            .orElseGet(() -> {
                                Class newClass = new Class(packageEntity, className, fullQualifiedName, null);
                                return classRepository.save(newClass);
                            });

                        // 重複除去のためSetを使用
                        packageClassSetMap.computeIfAbsent(packageName, k -> new java.util.HashSet<>()).add(classEntity);

                        // 基本型依存関係の解析
                        basicTypeDependencyAnalyzer.analyze(javaFile, cu, classEntity, analysisBatchId);
                    });
                });
            });
        }

        // 解析結果のサマリを作成
        List<PackageSummaryDto> packageSummaries = packageClassSetMap.entrySet().stream()
            .map(entry -> {
                String packageName = entry.getKey();
                java.util.Set<Class> classSet = entry.getValue();
                List<Class> classes = new ArrayList<>(classSet);
                
                // 依存関係の種類別件数を集計
                List<String> classFqns = classes.stream()
                    .map(Class::getFullQualifiedName)
                    .collect(Collectors.toList());
                
                Map<String, Long> dependencyKindCounts = classDependencyRepository
                    .findByAnalysisBatchId(analysisBatchId).stream()
                    .filter(dep -> classFqns.contains(dep.getSourceClassFqn()))
                    .collect(Collectors.groupingBy(
                        ClassDependency::getDependencyKindCode,
                        Collectors.counting()
                    ));
                
                // クラス名の重複除去（表示用）
                List<String> uniqueClassNames = classes.stream()
                    .map(Class::getName)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
                
                return new PackageSummaryDto(
                    packageName,
                    uniqueClassNames.size(),
                    uniqueClassNames,
                    dependencyKindCounts
                );
            })
            .collect(Collectors.toList());

        return new AnalysisResultDto(targetProjectPath, packageSummaries);
    }
}

