package com.example.springbootprojectanalyser.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * JavaParserユーティリティ
 * JavaParserの共通処理を提供する
 */
public final class JavaParserUtil {

    private static final JavaParser JAVA_PARSER = new JavaParser();

    private JavaParserUtil() {
        // ユーティリティクラスはインスタンス化しない
    }

    /**
     * JavaファイルをパースしてCompilationUnitを取得する
     *
     * @param filePath ファイルパス
     * @return CompilationUnit（パース失敗時は空）
     */
    public static Optional<CompilationUnit> parse(Path filePath) {
        try {
            ParseResult<CompilationUnit> result = JAVA_PARSER.parse(filePath);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                return result.getResult();
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * ソースコード文字列をパースしてCompilationUnitを取得する
     *
     * @param sourceCode ソースコード
     * @return CompilationUnit（パース失敗時は空）
     */
    public static Optional<CompilationUnit> parse(String sourceCode) {
        ParseResult<CompilationUnit> result = JAVA_PARSER.parse(sourceCode);
        if (result.isSuccessful() && result.getResult().isPresent()) {
            return result.getResult();
        }
        return Optional.empty();
    }
}

