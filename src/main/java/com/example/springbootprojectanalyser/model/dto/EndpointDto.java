package com.example.springbootprojectanalyser.model.dto;

/**
 * エンドポイントDTO
 */
public record EndpointDto(
    String endpointId,
    Long classId,
    String className,
    String uri,
    Long httpMethodId,
    String httpMethodName
) {
}

