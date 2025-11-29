package com.example.springbootprojectanalyser.model.dto;

import java.util.List;
import java.util.Map;

/**
 * パッケージサマリDTO
 */
public record PackageSummaryDto(
    String packageName,
    int classCount,
    List<String> classNames,
    Map<String, Long> dependencyKindCounts
) {
}

