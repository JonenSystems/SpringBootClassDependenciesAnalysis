package com.example.springbootprojectanalyser.repository.impl;

import com.example.springbootprojectanalyser.model.entity.Package;
import com.example.springbootprojectanalyser.model.entity.Project;
import com.example.springbootprojectanalyser.repository.PackageRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * パッケージリポジトリ実装クラス
 */
@Repository
public interface PackageRepositoryImpl extends JpaRepository<Package, Long>, PackageRepository {

    /**
     * プロジェクトに属するパッケージを検索する
     *
     * @param project プロジェクト
     * @return パッケージリスト
     */
    @Query("SELECT p FROM Package p WHERE p.project = :project")
    List<Package> findByProject(@Param("project") Project project);

    /**
     * フルネームでパッケージを検索する
     *
     * @param fullName フルネーム
     * @param project プロジェクト
     * @return パッケージ（存在しない場合は空）
     */
    @Query("SELECT p FROM Package p WHERE p.fullName = :fullName AND p.project = :project")
    Optional<Package> findByFullNameAndProject(@Param("fullName") String fullName, @Param("project") Project project);
}

