package com.example.springbootprojectanalyser.util;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Symbol Solverファクトリークラス
 * JavaParserのSymbol Solverを設定・生成する
 */
public class SymbolSolverFactory {

    /**
     * プロジェクトルートからSymbol Solverを生成する
     * @param projectRoot プロジェクトルートパス
     * @return JavaSymbolSolver
     */
    public static JavaSymbolSolver createSymbolSolver(Path projectRoot) {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();

        // Java標準ライブラリの型解決
        typeSolver.add(new ReflectionTypeSolver());

        // プロジェクト内のソースコードの型解決
        typeSolver.add(new JavaParserTypeSolver(projectRoot.toFile()));

        return new JavaSymbolSolver(typeSolver);
    }

    /**
     * プロジェクトルートパス（文字列）からSymbol Solverを生成する
     * @param projectRootPath プロジェクトルートパス（文字列）
     * @return JavaSymbolSolver
     */
    public static JavaSymbolSolver createSymbolSolver(String projectRootPath) {
        Path projectRoot = Paths.get(projectRootPath);
        return createSymbolSolver(projectRoot);
    }
}

