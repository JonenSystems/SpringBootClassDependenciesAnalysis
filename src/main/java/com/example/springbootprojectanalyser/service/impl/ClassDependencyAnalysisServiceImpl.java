package com.example.springbootprojectanalyser.service.impl;

import com.example.springbootprojectanalyser.model.dto.AnalysisExecutionDto;
import com.example.springbootprojectanalyser.model.dto.AnalysisResultDto;
import com.example.springbootprojectanalyser.model.dto.PackageSummaryDto;
import com.example.springbootprojectanalyser.model.entity.*;
import com.example.springbootprojectanalyser.repository.*;
import com.example.springbootprojectanalyser.service.ClassDependencyAnalysisService;
import com.example.springbootprojectanalyser.util.SymbolSolverFactory;
import com.example.springbootprojectanalyser.util.TypeResolver;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * クラス依存関係解析サービス実装クラス
 */
@Service
public class ClassDependencyAnalysisServiceImpl implements ClassDependencyAnalysisService {

    private final ProjectRepository projectRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ClassDependencyRepository classDependencyRepository;
    private final DependencyKindRepository dependencyKindRepository;

    public ClassDependencyAnalysisServiceImpl(
            ProjectRepository projectRepository,
            PackageInfoRepository packageInfoRepository,
            ClassEntityRepository classEntityRepository,
            ClassDependencyRepository classDependencyRepository,
            DependencyKindRepository dependencyKindRepository) {
        this.projectRepository = projectRepository;
        this.packageInfoRepository = packageInfoRepository;
        this.classEntityRepository = classEntityRepository;
        this.classDependencyRepository = classDependencyRepository;
        this.dependencyKindRepository = dependencyKindRepository;
    }

    @Override
    @Transactional
    public AnalysisResultDto executeAnalysis(AnalysisExecutionDto executionDto) {
        String targetProjectPath = executionDto.targetProjectPath();
        // TODO: targetPackagePatternは将来の実装で使用予定
        @SuppressWarnings("unused")
        String targetPackagePattern = executionDto.targetPackagePattern();

        // パスの検証
        Path projectRoot = Paths.get(targetProjectPath);
        if (!Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
            throw new IllegalArgumentException("指定されたパスが存在しないか、ディレクトリではありません: " + targetProjectPath);
        }

        // 既存データを削除
        classDependencyRepository.deleteAll();
        classEntityRepository.deleteAll();
        packageInfoRepository.deleteAll();
        projectRepository.deleteAll();

        // プロジェクトを作成
        Project project = new Project(targetProjectPath);
        project = projectRepository.save(project);

        // Javaファイルを収集
        List<Path> javaFiles = collectJavaFiles(projectRoot);
        System.out.println("Found " + javaFiles.size() + " Java files");

        if (javaFiles.isEmpty()) {
            throw new IllegalArgumentException("Javaファイルが見つかりませんでした: " + targetProjectPath);
        }

        // パッケージとクラスを解析・登録
        Map<String, PackageInfo> packageMap = new HashMap<>();
        Map<String, ClassEntity> classMap = new HashMap<>();

        int parsedCount = 0;
        int errorCount = 0;
        for (Path javaFile : javaFiles) {
            try {
                parseAndRegister(javaFile, project, projectRoot, packageMap, classMap);
                parsedCount++;
            } catch (Exception e) {
                // パースエラーはログに記録してスキップ
                errorCount++;
                System.err.println("Failed to parse: " + javaFile + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Parsed: " + parsedCount + ", Errors: " + errorCount);
        System.out.println("Packages: " + packageMap.size() + ", Classes: " + classMap.size());

        // Symbol Solverを生成
        JavaSymbolSolver symbolSolver = SymbolSolverFactory.createSymbolSolver(projectRoot);

        // 依存関係を解析
        parseDependencies(javaFiles, projectRoot, classMap, symbolSolver);

        return getAnalysisResult(targetProjectPath);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisResultDto getAnalysisResult(String projectPath) {
        Project project = projectRepository.findByRootPath(projectPath)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectPath));

        List<PackageInfo> packages = packageInfoRepository.findByProject(project);
        
        List<PackageSummaryDto> packageSummaries = packages.stream()
                .map(pkg -> createPackageSummary(pkg))
                .sorted(Comparator.comparing(PackageSummaryDto::packageName))
                .collect(Collectors.toList());

        return new AnalysisResultDto(projectPath, packageSummaries);
    }

    private List<Path> collectJavaFiles(Path root) {
        List<Path> javaFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains("target")) // targetディレクトリを除外
                    .filter(p -> !p.toString().contains(".git")) // .gitディレクトリを除外
                    .forEach(javaFiles::add);
        } catch (Exception e) {
            throw new RuntimeException("Failed to collect Java files from: " + root, e);
        }
        return javaFiles;
    }

    private void parseAndRegister(Path javaFile, Project project, Path projectRoot,
                                  Map<String, PackageInfo> packageMap,
                                  Map<String, ClassEntity> classMap) throws Exception {
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(javaFile).getResult().orElseThrow();

        // パッケージ情報を取得・登録
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");
        
        // パッケージ名が空の場合は"<default>"として扱う
        String displayPackageName = packageName.isEmpty() ? "<default>" : packageName;
        
        PackageInfo packageInfo = packageMap.computeIfAbsent(displayPackageName, name -> {
            String[] parts = name.equals("<default>") ? new String[]{"<default>"} : name.split("\\.");
            String simpleName = parts[parts.length - 1];
            PackageInfo pkg = new PackageInfo(project, name, simpleName);
            return packageInfoRepository.save(pkg);
        });

        // クラスを登録
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String fullQualifiedName = packageName.isEmpty() 
                    ? className 
                    : packageName + "." + className;
            
            // パッケージ名が空の場合でも、表示用パッケージ名を使用してマップに登録
            String mapKey = packageName.isEmpty() 
                    ? "<default>." + className 
                    : fullQualifiedName;
            
            if (!classMap.containsKey(mapKey)) {
                ClassEntity classEntity = new ClassEntity(project, packageInfo, fullQualifiedName, className);
                classEntity = classEntityRepository.save(classEntity);
                classMap.put(mapKey, classEntity);
            }
        });
    }

    private void parseDependencies(List<Path> javaFiles, Path projectRoot,
                                   Map<String, ClassEntity> classMap,
                                   JavaSymbolSolver symbolSolver) {
        // JavaParserの設定でSymbol Solverを有効化
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(symbolSolver);
        
        for (Path javaFile : javaFiles) {
            try {
                JavaParser parser = new JavaParser(parserConfiguration);
                CompilationUnit cu = parser.parse(javaFile).getResult().orElseThrow();
                
                String packageName = cu.getPackageDeclaration()
                        .map(pd -> pd.getNameAsString())
                        .orElse("");

                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                    String className = classDecl.getNameAsString();
                    String sourceFqn = packageName.isEmpty() 
                            ? className 
                            : packageName + "." + className;
                    
                    // パッケージ名が空の場合のマップキー
                    String mapKey = packageName.isEmpty() 
                            ? "<default>." + className 
                            : sourceFqn;
                    
                    ClassEntity sourceClass = classMap.get(mapKey);
                    if (sourceClass == null) {
                        return;
                    }

                    // 001_001: 継承（extends）
                    classDecl.getExtendedTypes().forEach(extendedType -> {
                        String targetFqn = TypeResolver.resolveFullyQualifiedName(extendedType, cu, packageName, classMap, symbolSolver);
                        if (targetFqn != null && !targetFqn.isEmpty() && !isPrimitiveOrBasicType(targetFqn)) {
                            saveDependency(sourceClass, sourceFqn, targetFqn, "001_001");
                        }
                    });

                    // 001_002: 実装（implements）
                    classDecl.getImplementedTypes().forEach(implType -> {
                        String targetFqn = TypeResolver.resolveFullyQualifiedName(implType, cu, packageName, classMap, symbolSolver);
                        if (targetFqn != null && !targetFqn.isEmpty() && !isPrimitiveOrBasicType(targetFqn)) {
                            saveDependency(sourceClass, sourceFqn, targetFqn, "001_002");
                        }
                    });

                    // 001_004: 例外型依存（throws句とcatch節）
                    // throws句の例外型依存
                    classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                        method.getThrownExceptions().forEach(exceptionType -> {
                            String targetFqn = TypeResolver.resolveFullyQualifiedName(exceptionType, cu, packageName, classMap, symbolSolver);
                            if (targetFqn != null && !targetFqn.isEmpty() && !isPrimitiveOrBasicType(targetFqn)) {
                                saveDependency(sourceClass, sourceFqn, targetFqn, "001_004");
                            }
                        });
                    });
                    
                    // catch節の例外型依存
                    classDecl.findAll(CatchClause.class).forEach(catchClause -> {
                        com.github.javaparser.ast.body.Parameter param = catchClause.getParameter();
                        if (param != null) {
                            Type exceptionType = param.getType();
                            if (exceptionType != null) {
                                String targetFqn = TypeResolver.resolveFullyQualifiedName(exceptionType, cu, packageName, classMap, symbolSolver);
                                if (targetFqn != null && !targetFqn.isEmpty() && !isPrimitiveOrBasicType(targetFqn)) {
                                    saveDependency(sourceClass, sourceFqn, targetFqn, "001_004");
                                }
                            }
                        }
                    });

                    // 001_006: 戻り値型依存（このクラス内のメソッドのみ）
                    classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                        Type returnType = method.getType();
                        if (returnType != null && !returnType.isVoidType() && !returnType.isPrimitiveType()) {
                            String targetFqn = TypeResolver.resolveFullyQualifiedName(returnType, cu, packageName, classMap, symbolSolver);
                            if (targetFqn != null && !targetFqn.isEmpty() && !isPrimitiveOrBasicType(targetFqn)) {
                                saveDependency(sourceClass, sourceFqn, targetFqn, "001_006");
                            }
                        }
                    });

                    // 001_003, 001_010: ジェネリクス型参照、集合保持（メソッドの戻り値型から抽出）
                    classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                        Type returnType = method.getType();
                        if (returnType != null && !returnType.isVoidType() && !returnType.isPrimitiveType()) {
                            extractGenericTypes(returnType, cu, packageName, classMap, symbolSolver).forEach(genericType -> {
                                if (genericType != null && !genericType.isEmpty() && !isPrimitiveOrBasicType(genericType)) {
                                    saveDependency(sourceClass, sourceFqn, genericType, "001_003");
                                }
                            });
                        }
                    });

                    // 001_007: 引数型依存（このクラス内のメソッドのみ）
                    classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                        method.getParameters().forEach(param -> {
                            Type paramType = param.getType();
                            if (paramType != null && !paramType.isPrimitiveType()) {
                                String targetFqn = TypeResolver.resolveFullyQualifiedName(paramType, cu, packageName, classMap, symbolSolver);
                                if (targetFqn != null && !targetFqn.isEmpty() && !isPrimitiveOrBasicType(targetFqn)) {
                                    saveDependency(sourceClass, sourceFqn, targetFqn, "001_007");
                                }
                            }
                        });
                    });

                    // 001_003, 001_010: ジェネリクス型参照、集合保持（メソッドの引数型から抽出）
                    classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                        method.getParameters().forEach(param -> {
                            Type paramType = param.getType();
                            if (paramType != null && !paramType.isPrimitiveType()) {
                                extractGenericTypes(paramType, cu, packageName, classMap, symbolSolver).forEach(genericType -> {
                                    if (genericType != null && !genericType.isEmpty() && !isPrimitiveOrBasicType(genericType)) {
                                        saveDependency(sourceClass, sourceFqn, genericType, "001_003");
                                    }
                                });
                            }
                        });
                    });

                    // 001_005: メソッド呼び出し（このクラス内のメソッド呼び出しのみ）
                    classDecl.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        Optional<Expression> scope = methodCall.getScope();
                        if (scope.isPresent()) {
                            Expression scopeExpr = scope.get();
                            String targetClassName = extractClassNameFromScope(scopeExpr, classDecl, cu, packageName, classMap);
                            if (targetClassName != null && !targetClassName.isEmpty() 
                                    && !isPrimitiveOrBasicType(targetClassName)
                                    && !targetClassName.equals(className)) { // 自分自身の呼び出しは除外
                                saveDependency(sourceClass, sourceFqn, targetClassName, "001_005");
                            }
                        }
                    });

                    // 001_008: 静的メソッド依存（このクラス内の静的メソッド呼び出しのみ）
                    classDecl.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        Optional<Expression> scope = methodCall.getScope();
                        if (scope.isPresent()) {
                            Expression scopeExpr = scope.get();
                            String staticClassName = extractStaticClassNameFromScope(scopeExpr, classDecl, cu, packageName, classMap);
                            if (staticClassName != null && !staticClassName.isEmpty() 
                                    && !isPrimitiveOrBasicType(staticClassName)
                                    && !staticClassName.equals(className)) { // 自分自身の呼び出しは除外
                                saveDependency(sourceClass, sourceFqn, staticClassName, "001_008");
                            }
                        }
                    });

                    // 001_011: 定数参照（このクラス内の定数参照のみ）
                    // メソッド呼び出しのスコープとして使用されているFieldAccessExprを除外するため、
                    // まず全てのMethodCallExprのスコープを収集
                    Set<Expression> methodCallScopes = new HashSet<>();
                    classDecl.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        methodCall.getScope().ifPresent(methodCallScopes::add);
                    });
                    
                    classDecl.findAll(FieldAccessExpr.class).forEach(fieldAccess -> {
                        // メソッド呼び出しのスコープとして使用されている場合は除外
                        if (methodCallScopes.contains(fieldAccess)) {
                            return;
                        }
                        
                        String constantClassName = extractConstantClassName(fieldAccess, classDecl, cu, packageName, classMap);
                        if (constantClassName != null && !constantClassName.isEmpty() 
                                && !isPrimitiveOrBasicType(constantClassName)
                                && !constantClassName.equals(className)) { // 自分自身の定数参照は除外
                            saveDependency(sourceClass, sourceFqn, constantClassName, "001_011");
                        }
                    });

                    // 001_009: コンポジション（保持）（このクラス内のフィールドのみ）
                    classDecl.findAll(FieldDeclaration.class).forEach(field -> {
                        Type fieldType = field.getCommonType();
                        if (fieldType != null && !fieldType.isPrimitiveType()) {
                            String targetFqn = TypeResolver.resolveFullyQualifiedName(fieldType, cu, packageName, classMap, symbolSolver);
                            if (targetFqn != null && !targetFqn.isEmpty() && !isPrimitiveOrBasicType(targetFqn)) {
                                saveDependency(sourceClass, sourceFqn, targetFqn, "001_009");
                            }
                        }
                    });

                    // 001_003, 001_010: ジェネリクス型参照、集合保持（このクラス内のフィールドのみ）
                    classDecl.findAll(FieldDeclaration.class).forEach(field -> {
                        Type fieldType = field.getCommonType();
                        extractGenericTypes(fieldType, cu, packageName, classMap, symbolSolver).forEach(genericType -> {
                            if (genericType != null && !genericType.isEmpty() && !isPrimitiveOrBasicType(genericType)) {
                                saveDependency(sourceClass, sourceFqn, genericType, "001_003");
                            }
                        });
                    });
                });
            } catch (Exception e) {
                System.err.println("Failed to parse dependencies: " + javaFile + " - " + e.getMessage());
            }
        }
    }


    /**
     * ジェネリクス型引数を抽出する
     * @param type 型
     * @param cu CompilationUnit（型解決に使用）
     * @param packageName 現在のパッケージ名
     * @param classMap プロジェクト内のクラス情報（型解決に使用）
     * @param symbolSolver JavaSymbolSolver（Symbol Solverを使用する場合）
     * @return ジェネリクス型引数のリスト
     */
    private List<String> extractGenericTypes(Type type, CompilationUnit cu, String packageName, Map<String, ClassEntity> classMap, JavaSymbolSolver symbolSolver) {
        List<String> genericTypes = new ArrayList<>();
        if (type.isClassOrInterfaceType()) {
            type.asClassOrInterfaceType().getTypeArguments().ifPresent(args -> {
                args.forEach(arg -> {
                    if (arg.isClassOrInterfaceType()) {
                        String genericType = TypeResolver.resolveFullyQualifiedName(arg, cu, packageName, classMap, symbolSolver);
                        if (genericType != null && !genericType.isEmpty()) {
                            genericTypes.add(genericType);
                        }
                    }
                });
            });
        }
        return genericTypes;
    }

    /**
     * メソッド呼び出しのスコープからクラス名を抽出する
     * @param scopeExpr スコープ式
     * @param classDecl クラス宣言（フィールドの型解決に使用）
     * @param cu CompilationUnit（型解決に使用）
     * @param packageName 現在のパッケージ名
     * @param classMap プロジェクト内のクラス情報（型解決に使用）
     * @return クラス名（完全修飾名または簡易名）、またはnull（自分自身の呼び出しなど）
     */
    private String extractClassNameFromScope(Expression scopeExpr, ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, String packageName, Map<String, ClassEntity> classMap) {
        if (scopeExpr instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccess = (FieldAccessExpr) scopeExpr;
            Expression scopeValue = fieldAccess.getScope();
            
            if (scopeValue != null) {
                
                // 静的メソッド呼び出しの場合（ClassName.staticMethod()）
                if (scopeValue instanceof NameExpr) {
                    String className = ((NameExpr) scopeValue).getNameAsString();
                    // フィールドとして存在しない場合、クラス名として扱う
                    boolean isField = classDecl.getFields().stream()
                            .anyMatch(f -> f.getVariables().stream()
                                    .anyMatch(v -> v.getNameAsString().equals(className)));
                    if (!isField) {
                        return className; // 静的メソッド呼び出しのクラス名
                    }
                }
                
                // インスタンスメソッド呼び出しの場合（obj.method()）
                if (scopeValue instanceof NameExpr) {
                    String fieldName = ((NameExpr) scopeValue).getNameAsString();
                    // this, superの場合は除外
                    if (fieldName.equals("this") || fieldName.equals("super")) {
                        return null;
                    }
                    
                    // フィールド名の場合、そのフィールドの型を取得
                    Optional<com.github.javaparser.ast.body.FieldDeclaration> field = classDecl.getFields().stream()
                            .filter(f -> f.getVariables().stream()
                                    .anyMatch(v -> v.getNameAsString().equals(fieldName)))
                            .findFirst();
                    if (field.isPresent()) {
                        Type fieldType = field.get().getCommonType();
                        String typeName = TypeResolver.resolveFullyQualifiedName(fieldType, cu, packageName, classMap);
                        if (typeName != null && !typeName.isEmpty() && !isPrimitiveOrBasicType(typeName)) {
                            return typeName;
                        }
                    }
                } else if (scopeValue instanceof FieldAccessExpr) {
                    // ネストしたフィールドアクセス（this.field.method()など）
                    return extractClassNameFromScope(scopeValue, classDecl, cu, packageName, classMap);
                }
            }
            
            // FieldAccessExprの全体を文字列として取得
            String scopeStr = scopeExpr.toString();
            if (scopeStr.contains(".")) {
                String[] parts = scopeStr.split("\\.");
                // 最初の部分がクラス名の可能性が高い
                if (parts.length > 0) {
                    return parts[0];
                }
            }
        } else if (scopeExpr instanceof NameExpr) {
            // NameExprの場合、変数名またはクラス名として扱う
            String name = ((NameExpr) scopeExpr).getNameAsString();
            
            // this, superの場合は除外
            if (name.equals("this") || name.equals("super")) {
                return null;
            }
            
            // フィールド名として検索
            Optional<com.github.javaparser.ast.body.FieldDeclaration> field = classDecl.getFields().stream()
                    .filter(f -> f.getVariables().stream()
                            .anyMatch(v -> v.getNameAsString().equals(name)))
                    .findFirst();
            if (field.isPresent()) {
                Type fieldType = field.get().getCommonType();
                String typeName = TypeResolver.resolveFullyQualifiedName(fieldType, cu, packageName, classMap);
                if (typeName != null && !typeName.isEmpty() && !isPrimitiveOrBasicType(typeName)) {
                    return typeName;
                }
            }
            
            // フィールドでもない場合、クラス名として扱う（静的メソッド呼び出しの可能性）
            // インポート文から解決を試みる
            return TypeResolver.resolveFromImports(name, cu, classMap)
                    .orElse(name);
        }
        
        // その他の場合は、toString()で文字列表現を取得
        String scopeStr = scopeExpr.toString();
        if (scopeStr.contains(".")) {
            // パッケージ名を含む場合、最初の部分をクラス名として扱う
            String[] parts = scopeStr.split("\\.");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return scopeStr;
    }

    /**
     * 静的メソッド呼び出しのスコープからクラス名を抽出する
     * 静的メソッド呼び出し（ClassName.staticMethod()）を識別
     * @param scopeExpr スコープ式
     * @param classDecl クラス宣言（フィールドの型解決に使用）
     * @param cu CompilationUnit（型解決に使用）
     * @param packageName 現在のパッケージ名
     * @param classMap プロジェクト内のクラス情報（型解決に使用）
     * @return クラス名（静的メソッド呼び出しの場合のみ）、またはnull
     */
    private String extractStaticClassNameFromScope(Expression scopeExpr, ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, String packageName, Map<String, ClassEntity> classMap) {
        if (scopeExpr instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccess = (FieldAccessExpr) scopeExpr;
            Expression scope = fieldAccess.getScope();
            
            if (scope != null) {
                // 静的メソッド呼び出し: ClassName.staticMethod()
                if (scope instanceof NameExpr) {
                    String name = ((NameExpr) scope).getNameAsString();
                    // フィールドとして存在しない場合、クラス名として扱う（静的メソッド呼び出し）
                    boolean isField = classDecl.getFields().stream()
                            .anyMatch(f -> f.getVariables().stream()
                                    .anyMatch(v -> v.getNameAsString().equals(name)));
                    if (!isField && !name.equals("this") && !name.equals("super")) {
                        // インポート文から解決を試みる
                        return TypeResolver.resolveFromImports(name, cu, classMap)
                                .orElse(name);
                    }
                } else if (scope instanceof FieldAccessExpr) {
                    // 完全修飾クラス名（package.ClassName.staticMethod()）
                    FieldAccessExpr nestedAccess = (FieldAccessExpr) scope;
                    Expression nestedScope = nestedAccess.getScope();
                    if (nestedScope instanceof NameExpr) {
                        // パッケージ名.クラス名の形式
                        String packageOrClass = ((NameExpr) nestedScope).getNameAsString();
                        String className = nestedAccess.getNameAsString();
                        // フィールドとして存在しない場合、クラス名として扱う
                        boolean isField = classDecl.getFields().stream()
                                .anyMatch(f -> f.getVariables().stream()
                                        .anyMatch(v -> v.getNameAsString().equals(packageOrClass)));
                        if (!isField) {
                            // package.ClassName の形式で返す
                            return packageOrClass + "." + className;
                        }
                    }
                }
            }
        } else if (scopeExpr instanceof NameExpr) {
            // NameExprの場合、クラス名として扱う（静的メソッド呼び出しの可能性）
            String name = ((NameExpr) scopeExpr).getNameAsString();
            
            // this, superの場合は除外
            if (name.equals("this") || name.equals("super")) {
                return null;
            }
            
            // フィールド名として存在しない場合、クラス名として扱う（静的メソッド呼び出し）
            boolean isField = classDecl.getFields().stream()
                    .anyMatch(f -> f.getVariables().stream()
                            .anyMatch(v -> v.getNameAsString().equals(name)));
            if (!isField) {
                // インポート文から解決を試みる
                return TypeResolver.resolveFromImports(name, cu, classMap)
                        .orElse(name);
            }
        }
        
        return null; // 静的メソッド呼び出しではない
    }

    /**
     * 定数参照（OtherClass.CONST）からクラス名を抽出する
     * @param fieldAccess FieldAccessExpr（定数参照）
     * @param classDecl クラス宣言（フィールドの型解決に使用）
     * @param cu CompilationUnit（型解決に使用）
     * @param packageName 現在のパッケージ名
     * @param classMap プロジェクト内のクラス情報（型解決に使用）
     * @return クラス名（定数参照の場合のみ）、またはnull
     */
    private String extractConstantClassName(FieldAccessExpr fieldAccess, ClassOrInterfaceDeclaration classDecl, CompilationUnit cu, String packageName, Map<String, ClassEntity> classMap) {
        Expression scope = fieldAccess.getScope();
        String fieldName = fieldAccess.getNameAsString();
        
        // フィールド名が大文字で始まる（定数の命名規則）
        if (fieldName == null || fieldName.isEmpty() || !Character.isUpperCase(fieldName.charAt(0))) {
            return null;
        }
        
        if (scope instanceof NameExpr) {
            String className = ((NameExpr) scope).getNameAsString();
            
            // this, superの場合は除外
            if (className.equals("this") || className.equals("super")) {
                return null;
            }
            
            // 自分自身のクラスのフィールドかチェック
            boolean isLocalField = classDecl.getFields().stream()
                    .anyMatch(f -> f.getVariables().stream()
                            .anyMatch(v -> v.getNameAsString().equals(fieldName)));
            
            // ローカルフィールドではない場合、他クラスの定数参照として扱う
            if (!isLocalField) {
                // インポート文から解決を試みる（ワイルドカードインポートも含む）
                return TypeResolver.resolveFromImports(className, cu, classMap)
                        .orElse(className);
            }
        } else if (scope instanceof FieldAccessExpr) {
            // 完全修飾クラス名（package.ClassName.CONSTANT）
            FieldAccessExpr nestedAccess = (FieldAccessExpr) scope;
            Expression nestedScope = nestedAccess.getScope();
            
            if (nestedScope instanceof NameExpr) {
                String packageOrClass = ((NameExpr) nestedScope).getNameAsString();
                String className = nestedAccess.getNameAsString();
                
                // パッケージ名.クラス名の形式
                return packageOrClass + "." + className;
            }
        }
        
        return null; // 定数参照ではない
    }

    private boolean isPrimitiveOrBasicType(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return true;
        }
        // Javaの基本型とよくあるライブラリ型を除外
        String lower = typeName.toLowerCase();
        return lower.equals("string") || lower.equals("int") || lower.equals("integer") 
            || lower.equals("long") || lower.equals("double") || lower.equals("float")
            || lower.equals("boolean") || lower.equals("char") || lower.equals("byte")
            || lower.equals("short") || lower.equals("void") || lower.equals("object")
            || typeName.startsWith("java.lang.") && (typeName.equals("java.lang.String")
            || typeName.equals("java.lang.Integer") || typeName.equals("java.lang.Long")
            || typeName.equals("java.lang.Double") || typeName.equals("java.lang.Float")
            || typeName.equals("java.lang.Boolean") || typeName.equals("java.lang.Character")
            || typeName.equals("java.lang.Byte") || typeName.equals("java.lang.Short")
            || typeName.equals("java.lang.Object"));
    }

    private void saveDependency(ClassEntity sourceClass, String sourceFqn, String targetIdentifier, String kindCode) {
        DependencyKindEntity kind = dependencyKindRepository.findByCode(kindCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown dependency kind code: " + kindCode));
        ClassDependency dependency = new ClassDependency(sourceClass, sourceFqn, targetIdentifier, kind);
        classDependencyRepository.save(dependency);
    }

    private PackageSummaryDto createPackageSummary(PackageInfo packageInfo) {
        List<ClassEntity> classes = classEntityRepository.findByPackageInfo(packageInfo);
        
        // 重複除去（fullQualifiedNameで比較）
        Set<ClassEntity> uniqueClasses = new LinkedHashSet<>(classes);
        List<String> classNames = uniqueClasses.stream()
                .map(ClassEntity::getSimpleName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        int classCount = classNames.size();

        // 依存関係の種類別件数（依存種類コードでソート）
        Map<String, Long> dependencyKindCounts = new TreeMap<>();
        List<DependencyKindEntity> allKinds = dependencyKindRepository.findAll();
        for (DependencyKindEntity kind : allKinds) {
            long count = classDependencyRepository.countByPackageInfoAndDependencyKindCode(packageInfo, kind.getCode());
            if (count > 0) {
                dependencyKindCounts.put(kind.getCode(), count);
            }
        }

        return new PackageSummaryDto(
                packageInfo.getFullName(),
                classCount,
                classNames,
                dependencyKindCounts
        );
    }
}

