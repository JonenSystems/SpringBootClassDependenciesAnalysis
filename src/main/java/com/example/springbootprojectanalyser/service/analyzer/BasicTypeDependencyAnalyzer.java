package com.example.springbootprojectanalyser.service.analyzer;

import com.example.springbootprojectanalyser.model.entity.Class;
import com.github.javaparser.ast.CompilationUnit;
import java.nio.file.Path;

/**
 * 基本型依存関係解析サービスインターフェース
 */
public interface BasicTypeDependencyAnalyzer {

    /**
     * 基本型依存関係を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyze(Path javaFile, CompilationUnit compilationUnit, Class sourceClass, String analysisBatchId);

    /**
     * 継承（extends）を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeExtends(Path javaFile, CompilationUnit compilationUnit, Class sourceClass, String analysisBatchId);

    /**
     * 実装（implements）を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeImplements(Path javaFile, CompilationUnit compilationUnit, Class sourceClass, String analysisBatchId);

    /**
     * フィールド依存（コンポジション）を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeFieldDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * メソッド引数依存を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeMethodParameterDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * メソッド戻り値依存を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeMethodReturnTypeDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * メソッド呼び出し依存を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeMethodInvocationDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * ローカル変数依存を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeLocalVariableDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * 型パラメータ依存を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeTypeParameterDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * コンストラクタ呼び出し依存を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeConstructorInvocationDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * 配列要素型依存を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeArrayElementTypeDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);

    /**
     * ジェネリクス型参照（コレクション要素型）を解析する
     *
     * @param javaFile        Javaファイルのパス
     * @param compilationUnit コンパイル単位
     * @param sourceClass     解析対象クラス
     * @param analysisBatchId 解析バッチID
     * @return 検出された依存関係の数
     */
    int analyzeCollectionElementTypeDependency(Path javaFile, CompilationUnit compilationUnit, Class sourceClass,
            String analysisBatchId);
}
