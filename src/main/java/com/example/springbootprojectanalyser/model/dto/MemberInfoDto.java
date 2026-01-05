package com.example.springbootprojectanalyser.model.dto;

/**
 * メンバー情報DTO
 */
public record MemberInfoDto(
    String name,
    String returnType,
    String visibility,
    String memberTypeCode
) {
}

