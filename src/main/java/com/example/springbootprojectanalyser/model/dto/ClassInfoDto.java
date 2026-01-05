package com.example.springbootprojectanalyser.model.dto;

/**
 * クラス情報DTO
 */
public record ClassInfoDto(
    Long id,
    String fullQualifiedName,
    String simpleName
) {
}

