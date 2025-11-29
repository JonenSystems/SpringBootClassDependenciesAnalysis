package com.example.springbootprojectanalyser.repository.impl;

import com.example.springbootprojectanalyser.model.entity.Class;
import com.example.springbootprojectanalyser.model.entity.Package;
import com.example.springbootprojectanalyser.repository.ClassRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * クラスリポジトリ実装クラス
 */
@Repository
public interface ClassRepositoryImpl extends JpaRepository<Class, Long>, ClassRepository {

    /**
     * パッケージに属するクラスを検索する
     *
     * @param packageEntity パッケージ
     * @return クラスリスト
     */
    @Query("SELECT c FROM Class c WHERE c.packageEntity = :packageEntity")
    List<Class> findByPackage(@Param("packageEntity") Package packageEntity);

    /**
     * 完全修飾名でクラスを検索する
     *
     * @param fullQualifiedName 完全修飾名
     * @return クラス（存在しない場合は空）
     */
    Optional<Class> findByFullQualifiedName(String fullQualifiedName);
}

