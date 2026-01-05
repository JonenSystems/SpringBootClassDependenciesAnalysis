package com.example.springbootprojectanalyser.service;

import com.example.springbootprojectanalyser.model.dto.EndpointDto;

import java.util.List;

/**
 * エンドポイント抽出サービスインターフェース
 */
public interface EndpointExtractionService {
    /**
     * エンドポイント情報を抽出する
     * 
     * @param targetProjectPath 解析対象プロジェクトパス
     * @param targetPackagePattern 解析対象パッケージパターン
     * @return 抽出されたエンドポイント情報のリスト
     */
    List<EndpointDto> extractEndpoints(String targetProjectPath, String targetPackagePattern);
}

