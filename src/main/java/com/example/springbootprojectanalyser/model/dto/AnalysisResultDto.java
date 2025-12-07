package com.example.springbootprojectanalyser.model.dto;

import java.util.List;

/**
 * 解析結果DTO
 */
public record AnalysisResultDto(
    String projectPath,
    List<PackageSummaryDto> packageSummaries
) {
}

