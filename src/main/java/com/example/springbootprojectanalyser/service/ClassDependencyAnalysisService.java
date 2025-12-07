package com.example.springbootprojectanalyser.service;

import com.example.springbootprojectanalyser.model.dto.AnalysisExecutionDto;
import com.example.springbootprojectanalyser.model.dto.AnalysisResultDto;

/**
 * クラス依存関係解析サービスインターフェース
 */
public interface ClassDependencyAnalysisService {
    /**
     * 解析を実行する
     * @param executionDto 解析実行DTO
     * @return 解析結果DTO
     */
    AnalysisResultDto executeAnalysis(AnalysisExecutionDto executionDto);
    
    /**
     * 解析結果を取得する
     * @param projectPath プロジェクトパス
     * @return 解析結果DTO
     */
    AnalysisResultDto getAnalysisResult(String projectPath);
}

