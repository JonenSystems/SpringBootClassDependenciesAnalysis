package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.PackageInfo;
import com.example.springbootprojectanalyser.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * パッケージ情報リポジトリインターフェース
 */
@Repository
public interface PackageInfoRepository extends JpaRepository<PackageInfo, Long> {
    List<PackageInfo> findByProject(Project project);
    
    @Query("SELECT p FROM PackageInfo p WHERE p.project = :project AND p.fullName = :fullName")
    Optional<PackageInfo> findByProjectAndFullName(@Param("project") Project project, @Param("fullName") String fullName);
}

