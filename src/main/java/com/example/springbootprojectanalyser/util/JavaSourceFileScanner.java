package com.example.springbootprojectanalyser.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Javaソースファイルスキャナ
 * ファイルシステムからJavaファイルを走査する
 */
public final class JavaSourceFileScanner {

    private JavaSourceFileScanner() {
        // ユーティリティクラスはインスタンス化しない
    }

    /**
     * 指定されたパス配下のJavaファイルを走査する
     *
     * @param rootPath ルートパス
     * @param packagePattern パッケージパターン（Ant形式、例: "com.example..*"）
     * @return Javaファイルのパスリスト
     * @throws IOException ファイルアクセスエラー
     */
    public static List<Path> scanJavaFiles(String rootPath, String packagePattern) throws IOException {
        List<Path> javaFiles = new ArrayList<>();
        Path root = Paths.get(rootPath);

        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("指定されたパスが存在しないか、ディレクトリではありません: " + rootPath);
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> matchesPackagePattern(path, root, packagePattern))
                .forEach(javaFiles::add);
        }

        return javaFiles;
    }

    /**
     * パッケージパターンにマッチするかチェックする
     *
     * @param filePath ファイルパス
     * @param rootPath ルートパス
     * @param packagePattern パッケージパターン（Ant形式）
     * @return マッチする場合true
     */
    private static boolean matchesPackagePattern(Path filePath, Path rootPath, String packagePattern) {
        if (packagePattern == null || packagePattern.isBlank() || "**".equals(packagePattern)) {
            return true;
        }

        try {
            Path relativePath = rootPath.relativize(filePath);
            String relativePathStr = relativePath.toString().replace("\\", "/");
            
            // パッケージパスを抽出（src/main/java/以降の部分）
            int srcIndex = relativePathStr.indexOf("src/main/java/");
            if (srcIndex == -1) {
                srcIndex = relativePathStr.indexOf("src/");
            }
            if (srcIndex == -1) {
                return false;
            }

            String packagePath = relativePathStr.substring(srcIndex);
            packagePath = packagePath.substring(0, packagePath.lastIndexOf("/"));
            packagePath = packagePath.replace("/", ".");

            // Ant形式のパターンマッチング（簡易実装）
            return matchesAntPattern(packagePath, packagePattern);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ant形式のパターンマッチング（簡易実装）
     *
     * @param text テキスト
     * @param pattern パターン
     * @return マッチする場合true
     */
    private static boolean matchesAntPattern(String text, String pattern) {
        // 簡易実装: ".." を ".*" に変換して正規表現でマッチ
        String regex = pattern
            .replace(".", "\\.")
            .replace("**", ".*")
            .replace("*", "[^.]*");
        return text.matches(regex);
    }
}

