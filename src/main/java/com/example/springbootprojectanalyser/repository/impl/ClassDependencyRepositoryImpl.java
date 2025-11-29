package com.example.springbootprojectanalyser.repository.impl;

import com.example.springbootprojectanalyser.model.entity.ClassDependency;
import com.example.springbootprojectanalyser.repository.ClassDependencyRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 依存関係リポジトリ実装クラス
 */
@Repository
public interface ClassDependencyRepositoryImpl extends JpaRepository<ClassDependency, UUID>, ClassDependencyRepository {

    /**
     * 解析バッチIDで依存関係を検索する
     *
     * @param analysisBatchId 解析バッチID
     * @return 依存関係リスト
     */
    @Query("SELECT d FROM ClassDependency d WHERE d.analysisBatchId = :analysisBatchId")
    List<ClassDependency> findByAnalysisBatchId(@Param("analysisBatchId") String analysisBatchId);

    /**
     * 依存種類コードで依存関係を検索する
     *
     * @param dependencyKindCode 依存種類コード
     * @return 依存関係リスト
     */
    @Query("SELECT d FROM ClassDependency d WHERE d.dependencyKindCode = :dependencyKindCode")
    List<ClassDependency> findByDependencyKindCode(@Param("dependencyKindCode") String dependencyKindCode);
}

