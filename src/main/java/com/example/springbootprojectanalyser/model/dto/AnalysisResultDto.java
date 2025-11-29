package com.example.springbootprojectanalyser.model.dto;

import java.util.List;

/**
 * 解析結果DTO
 */
public record AnalysisResultDto(
    String targetProjectPath,
    List<PackageSummaryDto> packageSummaries
) {
}

