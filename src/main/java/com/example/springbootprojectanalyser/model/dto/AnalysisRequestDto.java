package com.example.springbootprojectanalyser.model.dto;

/**
 * 解析リクエストDTO
 */
public record AnalysisRequestDto(
    String targetProjectPath,
    String targetPackagePattern
) {
}

