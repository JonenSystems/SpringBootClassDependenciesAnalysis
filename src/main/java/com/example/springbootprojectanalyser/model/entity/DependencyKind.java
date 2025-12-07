package com.example.springbootprojectanalyser.model.entity;

/**
 * 依存タイプの列挙型
 */
public enum DependencyKind {
    /** 001_001: 継承（extends） */
    EXTENDS("001_001", "継承（extends）"),
    /** 001_002: 実装（implements） */
    IMPLEMENTS("001_002", "実装（implements）"),
    /** 001_003: ジェネリクス型参照 */
    GENERIC_TYPE_REFERENCE("001_003", "ジェネリクス型参照"),
    /** 001_004: 例外型依存 */
    EXCEPTION_TYPE("001_004", "例外型依存"),
    /** 001_005: メソッド呼び出し */
    METHOD_CALL("001_005", "メソッド呼び出し"),
    /** 001_006: 戻り値型依存 */
    RETURN_TYPE("001_006", "戻り値型依存"),
    /** 001_007: 引数型依存 */
    PARAMETER_TYPE("001_007", "引数型依存"),
    /** 001_008: 静的メソッド依存 */
    STATIC_METHOD("001_008", "静的メソッド依存"),
    /** 001_009: コンポジション（保持） */
    COMPOSITION("001_009", "コンポジション（保持）"),
    /** 001_010: 集合保持 */
    COLLECTION_TYPE("001_010", "集合保持"),
    /** 001_011: 定数参照 */
    CONSTANT_REFERENCE("001_011", "定数参照");

    private final String code;
    private final String description;

    DependencyKind(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DependencyKind fromCode(String code) {
        for (DependencyKind kind : values()) {
            if (kind.code.equals(code)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown dependency kind code: " + code);
    }
}

