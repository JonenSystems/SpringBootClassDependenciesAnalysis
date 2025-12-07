package com.example.springbootprojectanalyser.model.dto;

/**
 * 解析実行DTO
 */
public record AnalysisExecutionDto(
    String targetProjectPath,
    String targetPackagePattern
) {
}

