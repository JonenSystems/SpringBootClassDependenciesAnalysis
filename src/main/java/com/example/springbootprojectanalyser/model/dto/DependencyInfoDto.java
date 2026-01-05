package com.example.springbootprojectanalyser.model.dto;

/**
 * 依存関係情報DTO
 */
public record DependencyInfoDto(
    String targetFqn,
    String dependencyKindCode,
    String dependencyKindDescription
) {
}

