package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.ClassDependency;
import java.util.List;

/**
 * 依存関係リポジトリインターフェース
 * 
 * 注意: save, saveAll, findById は JpaRepository から継承されるため、
 * このインターフェースには定義していません。
 */
public interface ClassDependencyRepository {

    /**
     * 解析バッチIDで依存関係を検索する
     *
     * @param analysisBatchId 解析バッチID
     * @return 依存関係リスト
     */
    List<ClassDependency> findByAnalysisBatchId(String analysisBatchId);

    /**
     * 依存種類コードで依存関係を検索する
     *
     * @param dependencyKindCode 依存種類コード
     * @return 依存関係リスト
     */
    List<ClassDependency> findByDependencyKindCode(String dependencyKindCode);
}

