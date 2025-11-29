package com.example.springbootprojectanalyser.service.analyzer.impl;

import com.example.springbootprojectanalyser.model.entity.Class;
import com.example.springbootprojectanalyser.model.entity.ClassDependency;
import com.example.springbootprojectanalyser.repository.impl.ClassDependencyRepositoryImpl;
import com.example.springbootprojectanalyser.service.analyzer.BasicTypeDependencyAnalyzer;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 基本型依存関係解析サービス実装クラス
 */
@Service
public class BasicTypeDependencyAnalyzerImpl implements BasicTypeDependencyAnalyzer {

    private final ClassDependencyRepositoryImpl classDependencyRepository;

    public BasicTypeDependencyAnalyzerImpl(ClassDependencyRepositoryImpl classDependencyRepository) {
        this.classDependencyRepository = classDependencyRepository;
    }

    @Override
    public int analyze(Path javaFile, CompilationUnit compilationUnit, Class sourceClass, String analysisBatchId) {
        int count = 0;
        count += analyzeExtends(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeImplements(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeFieldDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeMethodParameterDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeMethodReturnTypeDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeLocalVariableDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeTypeParameterDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeMethodInvocationDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeConstructorInvocationDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeArrayElementTypeDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        count += analyzeCollectionElementTypeDependency(javaFile, compilationUnit, sourceClass, analysisBatchId);
        return count;
    }

    @Override
    public int analyzeExtends(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            // このクラス宣言が解析対象のクラスかチェック
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // 継承（extends）を抽出
            classDecl.getExtendedTypes().forEach(extendedType -> {
                String targetIdentifier = extendedType.getNameAsString();
                ClassDependency dependency = new ClassDependency(
                        sourceClassFqn,
                        targetIdentifier,
                        "001_001",
                        analysisBatchId);
                dependencies.add(dependency);
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeImplements(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            // このクラス宣言が解析対象のクラスかチェック
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // 実装（implements）を抽出
            classDecl.getImplementedTypes().forEach(implementedType -> {
                String targetIdentifier = implementedType.getNameAsString();
                ClassDependency dependency = new ClassDependency(
                        sourceClassFqn,
                        targetIdentifier,
                        "001_002",
                        analysisBatchId);
                dependencies.add(dependency);
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeFieldDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // フィールド依存（コンポジション）を抽出
            classDecl.getFields().forEach(field -> {
                Type fieldType = field.getCommonType();
                extractTypeName(fieldType).ifPresent(typeName -> {
                    // プリミティブ型やjava.langパッケージの型は除外
                    if (!isPrimitiveOrJavaLangType(typeName)) {
                        ClassDependency dependency = new ClassDependency(
                                sourceClassFqn,
                                typeName,
                                "001_003",
                                analysisBatchId);
                        dependencies.add(dependency);
                    }
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeMethodParameterDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // メソッド引数依存を抽出
            classDecl.getMethods().forEach(method -> {
                method.getParameters().forEach(parameter -> {
                    Type paramType = parameter.getType();
                    extractTypeName(paramType).ifPresent(typeName -> {
                        // プリミティブ型やjava.langパッケージの型は除外
                        if (!isPrimitiveOrJavaLangType(typeName)) {
                            ClassDependency dependency = new ClassDependency(
                                    sourceClassFqn,
                                    typeName,
                                    "001_004",
                                    analysisBatchId);
                            dependencies.add(dependency);
                        }
                    });
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeMethodReturnTypeDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // メソッド戻り値依存を抽出
            classDecl.getMethods().forEach(method -> {
                Type returnType = method.getType();
                // void型の場合はスキップ
                if (returnType instanceof com.github.javaparser.ast.type.VoidType) {
                    return;
                }
                extractTypeName(returnType).ifPresent(typeName -> {
                    // プリミティブ型やjava.langパッケージの型は除外
                    if (!isPrimitiveOrJavaLangType(typeName)) {
                        ClassDependency dependency = new ClassDependency(
                                sourceClassFqn,
                                typeName,
                                "001_005",
                                analysisBatchId);
                        dependencies.add(dependency);
                    }
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeMethodInvocationDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // メソッド呼び出し依存を抽出
            classDecl.findAll(MethodCallExpr.class).forEach(methodCall -> {
                methodCall.getScope().ifPresent(scope -> {
                    if (scope instanceof com.github.javaparser.ast.expr.NameExpr nameExpr) {
                        String targetIdentifier = nameExpr.getNameAsString();
                        ClassDependency dependency = new ClassDependency(
                                sourceClassFqn,
                                targetIdentifier,
                                "001_008",
                                analysisBatchId);
                        dependencies.add(dependency);
                    } else if (scope instanceof com.github.javaparser.ast.expr.FieldAccessExpr fieldAccess) {
                        extractTypeNameFromFieldAccess(fieldAccess).ifPresent(typeName -> {
                            if (!isPrimitiveOrJavaLangType(typeName)) {
                                ClassDependency dependency = new ClassDependency(
                                        sourceClassFqn,
                                        typeName,
                                        "001_008",
                                        analysisBatchId);
                                dependencies.add(dependency);
                            }
                        });
                    }
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeCollectionElementTypeDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // フィールドのジェネリクス型引数を抽出（コレクション要素型依存）
            classDecl.getFields().forEach(field -> {
                Type fieldType = field.getCommonType();
                if (fieldType instanceof ClassOrInterfaceType classOrInterfaceType) {
                    classOrInterfaceType.getTypeArguments().ifPresent(typeArgs -> {
                        typeArgs.forEach(typeArg -> {
                            extractTypeName(typeArg).ifPresent(typeName -> {
                                if (!isPrimitiveOrJavaLangType(typeName)) {
                                    ClassDependency dependency = new ClassDependency(
                                            sourceClassFqn,
                                            typeName,
                                            "001_011",
                                            analysisBatchId);
                                    dependencies.add(dependency);
                                }
                            });
                        });
                    });
                }
            });

            // メソッド引数のジェネリクス型引数を抽出
            classDecl.getMethods().forEach(method -> {
                method.getParameters().forEach(parameter -> {
                    Type paramType = parameter.getType();
                    if (paramType instanceof ClassOrInterfaceType classOrInterfaceType) {
                        classOrInterfaceType.getTypeArguments().ifPresent(typeArgs -> {
                            typeArgs.forEach(typeArg -> {
                                extractTypeName(typeArg).ifPresent(typeName -> {
                                    if (!isPrimitiveOrJavaLangType(typeName)) {
                                        ClassDependency dependency = new ClassDependency(
                                                sourceClassFqn,
                                                typeName,
                                                "001_010",
                                                analysisBatchId);
                                        dependencies.add(dependency);
                                    }
                                });
                            });
                        });
                    }
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeLocalVariableDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // メソッド内のローカル変数依存を抽出
            classDecl.getMethods().forEach(method -> {
                method.getBody().ifPresent(body -> {
                    body.findAll(com.github.javaparser.ast.expr.VariableDeclarationExpr.class).forEach(varDeclExpr -> {
                        // VariableDeclarationExprから型を取得
                        Type varType = varDeclExpr.getCommonType();
                        extractTypeName(varType).ifPresent(typeName -> {
                            if (!isPrimitiveOrJavaLangType(typeName)) {
                                ClassDependency dependency = new ClassDependency(
                                        sourceClassFqn,
                                        typeName,
                                        "001_006",
                                        analysisBatchId);
                                dependencies.add(dependency);
                            }
                        });
                    });
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeTypeParameterDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // クラスの型パラメータ依存を抽出
            classDecl.getTypeParameters().forEach(typeParam -> {
                typeParam.getTypeBound().forEach(boundType -> {
                    extractTypeName(boundType).ifPresent(typeName -> {
                        if (!isPrimitiveOrJavaLangType(typeName)) {
                            ClassDependency dependency = new ClassDependency(
                                    sourceClassFqn,
                                    typeName,
                                    "001_007",
                                    analysisBatchId);
                            dependencies.add(dependency);
                        }
                    });
                });
            });

            // メソッドの型パラメータ依存を抽出
            classDecl.getMethods().forEach(method -> {
                method.getTypeParameters().forEach(typeParam -> {
                    typeParam.getTypeBound().forEach(boundType -> {
                        extractTypeName(boundType).ifPresent(typeName -> {
                            if (!isPrimitiveOrJavaLangType(typeName)) {
                                ClassDependency dependency = new ClassDependency(
                                        sourceClassFqn,
                                        typeName,
                                        "001_007",
                                        analysisBatchId);
                                dependencies.add(dependency);
                            }
                        });
                    });
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeConstructorInvocationDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // コンストラクタ呼び出し依存を抽出
            classDecl.findAll(ObjectCreationExpr.class).forEach(objectCreation -> {
                ClassOrInterfaceType type = objectCreation.getType();
                extractTypeName(type).ifPresent(typeName -> {
                    if (!isPrimitiveOrJavaLangType(typeName)) {
                        ClassDependency dependency = new ClassDependency(
                                sourceClassFqn,
                                typeName,
                                "001_009",
                                analysisBatchId);
                        dependencies.add(dependency);
                    }
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    @Override
    public int analyzeArrayElementTypeDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId) {
        List<ClassDependency> dependencies = new ArrayList<>();
        String sourceClassFqn = sourceClass.getFullQualifiedName();

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            String fullQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            if (!fullQualifiedName.equals(sourceClassFqn)) {
                return;
            }

            // フィールドの配列要素型依存を抽出
            classDecl.getFields().forEach(field -> {
                Type fieldType = field.getCommonType();
                extractArrayElementType(fieldType).ifPresent(typeName -> {
                    if (!isPrimitiveOrJavaLangType(typeName)) {
                        ClassDependency dependency = new ClassDependency(
                                sourceClassFqn,
                                typeName,
                                "001_010",
                                analysisBatchId);
                        dependencies.add(dependency);
                    }
                });
            });

            // メソッド引数の配列要素型依存を抽出
            classDecl.getMethods().forEach(method -> {
                method.getParameters().forEach(parameter -> {
                    Type paramType = parameter.getType();
                    extractArrayElementType(paramType).ifPresent(typeName -> {
                        if (!isPrimitiveOrJavaLangType(typeName)) {
                            ClassDependency dependency = new ClassDependency(
                                    sourceClassFqn,
                                    typeName,
                                    "001_010",
                                    analysisBatchId);
                            dependencies.add(dependency);
                        }
                    });
                });
            });
        });

        if (!dependencies.isEmpty()) {
            classDependencyRepository.saveAll(dependencies);
        }

        return dependencies.size();
    }

    /**
     * 型から型名を抽出する
     *
     * @param type 型
     * @return 型名（完全修飾名を優先、存在する場合）
     */
    private Optional<String> extractTypeName(Type type) {
        if (type instanceof ClassOrInterfaceType classOrInterfaceType) {
            // 完全修飾名を取得（可能な場合）
            try {
                if (classOrInterfaceType.resolve().isReferenceType()) {
                    return Optional.of(classOrInterfaceType.resolve().asReferenceType().getQualifiedName());
                }
            } catch (Exception e) {
                // 解決できない場合は単純名を使用
            }
            // スコープがある場合は結合
            StringBuilder typeNameBuilder = new StringBuilder();
            classOrInterfaceType.getScope().ifPresent(scope -> {
                String scopeName = null;
                // Node型として扱い、型チェックを行う
                if (ClassOrInterfaceType.class.isInstance(scope)) {
                    ClassOrInterfaceType scopeType = ClassOrInterfaceType.class.cast(scope);
                    scopeName = scopeType.getNameAsString();
                } else if (com.github.javaparser.ast.expr.NameExpr.class.isInstance(scope)) {
                    com.github.javaparser.ast.expr.NameExpr nameExpr = com.github.javaparser.ast.expr.NameExpr.class
                            .cast(scope);
                    scopeName = nameExpr.getNameAsString();
                }
                if (scopeName != null) {
                    typeNameBuilder.append(scopeName).append(".");
                }
            });
            typeNameBuilder.append(classOrInterfaceType.getNameAsString());
            return Optional.of(typeNameBuilder.toString());
        }
        return Optional.empty();
    }

    /**
     * FieldAccessExprから型名を抽出する
     *
     * @param fieldAccess フィールドアクセス式
     * @return 型名（存在する場合）
     */
    private Optional<String> extractTypeNameFromFieldAccess(
            com.github.javaparser.ast.expr.FieldAccessExpr fieldAccess) {
        com.github.javaparser.ast.expr.Expression scope = fieldAccess.getScope();
        if (scope instanceof com.github.javaparser.ast.expr.NameExpr nameExpr) {
            return Optional.of(nameExpr.getNameAsString());
        }
        return Optional.empty();
    }

    /**
     * プリミティブ型またはjava.langパッケージの型かどうかを判定する
     *
     * @param typeName 型名
     * @return プリミティブ型またはjava.langパッケージの型の場合true
     */
    private boolean isPrimitiveOrJavaLangType(String typeName) {
        // プリミティブ型
        if (typeName.equals("int") || typeName.equals("long") || typeName.equals("double") ||
                typeName.equals("float") || typeName.equals("boolean") || typeName.equals("char") ||
                typeName.equals("byte") || typeName.equals("short")) {
            return true;
        }
        // java.langパッケージの型
        if (typeName.startsWith("java.lang.") || typeName.equals("String") ||
                typeName.equals("Object") || typeName.equals("Integer") ||
                typeName.equals("Long") || typeName.equals("Double") ||
                typeName.equals("Float") || typeName.equals("Boolean") ||
                typeName.equals("Character") || typeName.equals("Byte") ||
                typeName.equals("Short")) {
            return true;
        }
        return false;
    }

    /**
     * 配列型から要素型を抽出する
     *
     * @param type 型
     * @return 要素型名（存在する場合）
     */
    private Optional<String> extractArrayElementType(Type type) {
        if (type instanceof ArrayType arrayType) {
            Type componentType = arrayType.getComponentType();
            return extractTypeName(componentType);
        }
        return Optional.empty();
    }
}
