package com.example.springbootprojectanalyser.service;

import com.example.springbootprojectanalyser.model.dto.ClassDiagramDto;

import java.util.UUID;

/**
 * クラス図生成サービスインターフェース
 */
public interface ClassDiagramService {
    /**
     * クラス図を生成する
     * 
     * @param selectedEndpointId 選択されたエンドポイントID
     * @param projectId プロジェクトID
     * @return クラス図DTO
     */
    ClassDiagramDto generateClassDiagram(UUID selectedEndpointId, Long projectId);
}

