package com.example.springbootprojectanalyser.util;

import com.example.springbootprojectanalyser.model.entity.ClassEntity;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import java.util.*;

/**
 * 型解決ユーティリティクラス
 * JavaParserのASTから完全修飾名を解決する
 */
public class TypeResolver {

    /**
     * 型から完全修飾名を取得する
     * @param type 型
     * @param cu CompilationUnit（インポート文の解決に使用）
     * @param currentPackageName 現在のパッケージ名
     * @return 完全修飾名、解決できない場合は簡易名
     */
    public static String resolveFullyQualifiedName(Type type, CompilationUnit cu, String currentPackageName) {
        return resolveFullyQualifiedName(type, cu, currentPackageName, null);
    }

    /**
     * 型から完全修飾名を取得する（プロジェクト内クラス情報を使用）
     * @param type 型
     * @param cu CompilationUnit（インポート文の解決に使用）
     * @param currentPackageName 現在のパッケージ名
     * @param classMap プロジェクト内のクラス情報（ワイルドカードインポート解決に使用）
     * @return 完全修飾名、解決できない場合は簡易名
     */
    public static String resolveFullyQualifiedName(Type type, CompilationUnit cu, String currentPackageName, Map<String, ClassEntity> classMap) {
        return resolveFullyQualifiedName(type, cu, currentPackageName, classMap, null);
    }

    /**
     * 型から完全修飾名を取得する（Symbol Solver対応）
     * @param type 型
     * @param cu CompilationUnit（インポート文の解決に使用）
     * @param currentPackageName 現在のパッケージ名
     * @param classMap プロジェクト内のクラス情報（ワイルドカードインポート解決に使用）
     * @param symbolSolver JavaSymbolSolver（Symbol Solverを使用する場合、nullの場合は従来の方法を使用）
     * @return 完全修飾名、解決できない場合は簡易名
     */
    public static String resolveFullyQualifiedName(Type type, CompilationUnit cu, String currentPackageName, Map<String, ClassEntity> classMap, JavaSymbolSolver symbolSolver) {
        if (type == null) {
            return null;
        }

        // Symbol Solverが設定されている場合は優先的に使用
        if (symbolSolver != null) {
            String symbolSolverResult = resolveFullyQualifiedNameWithSymbolSolver(type, cu, symbolSolver);
            if (symbolSolverResult != null && !symbolSolverResult.isEmpty()) {
                return symbolSolverResult;
            }
        }

        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classOrInterfaceType = type.asClassOrInterfaceType();
            return resolveClassOrInterfaceTypeFQN(classOrInterfaceType, cu, currentPackageName, classMap);
        }

        // その他の型は文字列表現を返す
        return type.toString();
    }

    /**
     * Symbol Solverを使用して型から完全修飾名を取得する
     * @param type 型
     * @param cu CompilationUnit
     * @param symbolSolver JavaSymbolSolver（Symbol Solverを使用する場合、nullの場合は従来の方法を使用）
     * @return 完全修飾名、解決できない場合は簡易名
     */
    public static String resolveFullyQualifiedNameWithSymbolSolver(Type type, CompilationUnit cu, JavaSymbolSolver symbolSolver) {
        if (type == null) {
            return null;
        }

        // Symbol Solverが設定されている場合は使用
        if (symbolSolver != null) {
            try {
                if (type.isClassOrInterfaceType()) {
                    // Symbol Solverを使用して型を解決
                    com.github.javaparser.resolution.types.ResolvedType resolvedType = type.resolve();
                    if (resolvedType.isReferenceType()) {
                        Optional<com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration> typeDeclarationOpt = 
                                resolvedType.asReferenceType().getTypeDeclaration();
                        if (typeDeclarationOpt.isPresent()) {
                            com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration typeDeclaration = typeDeclarationOpt.get();
                            String qualifiedName = typeDeclaration.getQualifiedName();
                            if (qualifiedName != null && !qualifiedName.isEmpty()) {
                                return qualifiedName;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Symbol Solverで解決できない場合は、従来の方法で解決を試みる
                // エラーは無視して従来の方法にフォールバック
            }
        }

        // Symbol Solverが設定されていない場合、または解決に失敗した場合は従来の方法で解決
        String packageName = getPackageName(cu);
        return resolveFullyQualifiedName(type, cu, packageName, null);
    }

    /**
     * CompilationUnitからパッケージ名を取得する
     * @param cu CompilationUnit
     * @return パッケージ名
     */
    private static String getPackageName(CompilationUnit cu) {
        if (cu == null) {
            return "";
        }
        return cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");
    }

    /**
     * ClassOrInterfaceTypeから完全修飾名を解決する
     * @param type クラスまたはインターフェース型
     * @param cu CompilationUnit
     * @param currentPackageName 現在のパッケージ名
     * @param classMap プロジェクト内のクラス情報（ワイルドカードインポート解決に使用）
     * @return 完全修飾名、解決できない場合は簡易名
     */
    private static String resolveClassOrInterfaceTypeFQN(ClassOrInterfaceType type, 
                                                         CompilationUnit cu, 
                                                         String currentPackageName,
                                                         Map<String, ClassEntity> classMap) {
        String typeName = type.getNameAsString();
        
        // 既に完全修飾名の形式（ドットを含む）の場合はそのまま返す
        if (typeName.contains(".") && !typeName.startsWith("java.")) {
            // java.lang以外のパッケージの可能性があるが、確認が必要
            return typeName;
        }

        // インポート文から完全修飾名を解決（ワイルドカードインポートも含む）
        Optional<String> importedFQN = resolveFromImports(typeName, cu, classMap);
        if (importedFQN.isPresent()) {
            return importedFQN.get();
        }

        // java.langパッケージの型をチェック
        if (isJavaLangType(typeName)) {
            return "java.lang." + typeName;
        }

        // 現在のパッケージ内の型をチェック
        if (currentPackageName != null && !currentPackageName.isEmpty()) {
            String possibleFQN = currentPackageName + "." + typeName;
            return possibleFQN;
        }

        // 解決できない場合は簡易名を返す
        return typeName;
    }

    /**
     * インポート文から完全修飾名を解決する
     * @param simpleName 簡易名
     * @param cu CompilationUnit
     * @return 完全修飾名（解決できた場合）
     */
    public static Optional<String> resolveFromImports(String simpleName, CompilationUnit cu) {
        return resolveFromImports(simpleName, cu, null);
    }

    /**
     * インポート文から完全修飾名を解決する（ワイルドカードインポート対応）
     * @param simpleName 簡易名
     * @param cu CompilationUnit
     * @param classMap プロジェクト内のクラス情報（ワイルドカードインポート解決に使用）
     * @return 完全修飾名（解決できた場合）
     */
    public static Optional<String> resolveFromImports(String simpleName, CompilationUnit cu, Map<String, ClassEntity> classMap) {
        if (cu == null) {
            return Optional.empty();
        }

        // インポートマップを作成（簡易名 -> 完全修飾名）
        Map<String, String> importMap = new HashMap<>();
        List<String> wildcardImports = new ArrayList<>();
        
        for (ImportDeclaration importDecl : cu.getImports()) {
            String importName = importDecl.getNameAsString();
            
            // static importはスキップ
            if (importDecl.isStatic()) {
                continue;
            }

            // ワイルドカードインポート
            if (importName.endsWith(".*")) {
                String packageName = importName.substring(0, importName.length() - 2); // ".*"を削除
                wildcardImports.add(packageName);
                continue;
            }

            // 通常のインポート
            String[] parts = importName.split("\\.");
            if (parts.length > 0) {
                String importedSimpleName = parts[parts.length - 1];
                importMap.put(importedSimpleName, importName);
            }
        }

        // 通常のインポートから検索
        String fqn = importMap.get(simpleName);
        if (fqn != null) {
            return Optional.of(fqn);
        }

        // ワイルドカードインポートから検索（プロジェクト内クラス情報を使用）
        if (classMap != null && !wildcardImports.isEmpty()) {
            for (String wildcardPackage : wildcardImports) {
                String possibleFQN = wildcardPackage + "." + simpleName;
                
                // プロジェクト内のクラス情報から検索
                Optional<String> foundFQN = findClassInMap(possibleFQN, simpleName, wildcardPackage, classMap);
                if (foundFQN.isPresent()) {
                    return foundFQN;
                }
            }
        }

        return Optional.empty();
    }

    /**
     * プロジェクト内のクラス情報からクラスを検索する
     * @param possibleFQN 可能性のある完全修飾名
     * @param simpleName 簡易名
     * @param packageName パッケージ名
     * @param classMap プロジェクト内のクラス情報
     * @return 完全修飾名（見つかった場合）
     */
    private static Optional<String> findClassInMap(String possibleFQN, String simpleName, String packageName, Map<String, ClassEntity> classMap) {
        // 完全修飾名で直接検索
        if (classMap.containsKey(possibleFQN)) {
            return Optional.of(possibleFQN);
        }

        // パッケージ名のサブパッケージも検索
        // 例: com.example.util.* の場合、com.example.util.Class だけでなく
        // com.example.util.sub.Class も検索対象とする
        for (Map.Entry<String, ClassEntity> entry : classMap.entrySet()) {
            String fqn = entry.getKey();
            ClassEntity classEntity = entry.getValue();
            
            // 簡易名が一致し、パッケージ名が指定されたパッケージまたはそのサブパッケージの場合
            if (simpleName.equals(classEntity.getSimpleName())) {
                String classPackage = extractPackageFromFQN(fqn);
                if (classPackage != null && (classPackage.equals(packageName) || classPackage.startsWith(packageName + "."))) {
                    return Optional.of(fqn);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 完全修飾名からパッケージ名を抽出する
     * @param fqn 完全修飾名
     * @return パッケージ名（クラス名を含まない）
     */
    private static String extractPackageFromFQN(String fqn) {
        if (fqn == null || fqn.isEmpty()) {
            return null;
        }
        
        // <default>パッケージの場合はnullを返す
        if (fqn.startsWith("<default>.")) {
            return null;
        }
        
        int lastDotIndex = fqn.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fqn.substring(0, lastDotIndex);
        }
        
        return null;
    }

    /**
     * java.langパッケージの型かどうかを判定
     * @param typeName 型名
     * @return java.langパッケージの型の場合true
     */
    private static boolean isJavaLangType(String typeName) {
        // よく使われるjava.langの型
        return typeName.equals("String") ||
               typeName.equals("Integer") ||
               typeName.equals("Long") ||
               typeName.equals("Double") ||
               typeName.equals("Float") ||
               typeName.equals("Boolean") ||
               typeName.equals("Character") ||
               typeName.equals("Byte") ||
               typeName.equals("Short") ||
               typeName.equals("Object") ||
               typeName.equals("Exception") ||
               typeName.equals("RuntimeException") ||
               typeName.equals("Error") ||
               typeName.equals("Throwable") ||
               typeName.equals("Comparable") ||
               typeName.equals("Cloneable") ||
               typeName.equals("Iterable");
    }

    /**
     * 型が含まれるCompilationUnitを取得する
     * @param node ノード
     * @return CompilationUnit
     */
    public static Optional<CompilationUnit> findCompilationUnit(Node node) {
        Node parent = node.getParentNode().orElse(null);
        while (parent != null) {
            if (parent instanceof CompilationUnit) {
                return Optional.of((CompilationUnit) parent);
            }
            parent = parent.getParentNode().orElse(null);
        }
        return Optional.empty();
    }
}

