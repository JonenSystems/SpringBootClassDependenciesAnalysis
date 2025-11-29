package com.example.springbootprojectanalyser.service;

import com.example.springbootprojectanalyser.model.dto.AnalysisRequestDto;
import com.example.springbootprojectanalyser.model.dto.AnalysisResultDto;

/**
 * 解析サービスインターフェース
 */
public interface AnalysisService {

    /**
     * 解析を実行する
     *
     * @param request 解析リクエスト
     * @return 解析結果
     */
    AnalysisResultDto executeAnalysis(AnalysisRequestDto request);
}

