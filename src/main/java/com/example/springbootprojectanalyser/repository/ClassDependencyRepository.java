package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.ClassDependency;
import com.example.springbootprojectanalyser.model.entity.PackageInfo;
import com.example.springbootprojectanalyser.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * クラス依存関係リポジトリインターフェース
 */
@Repository
public interface ClassDependencyRepository extends JpaRepository<ClassDependency, String> {
    @Query("SELECT d FROM ClassDependency d WHERE d.sourceClass.project = :project")
    List<ClassDependency> findByProject(@Param("project") Project project);
    
    @Query("SELECT d FROM ClassDependency d WHERE d.sourceClass.packageInfo = :packageInfo")
    List<ClassDependency> findByPackageInfo(@Param("packageInfo") PackageInfo packageInfo);
    
    @Query("SELECT d FROM ClassDependency d WHERE d.sourceClass.project = :project AND d.dependencyKind.code = :dependencyKindCode")
    List<ClassDependency> findByProjectAndDependencyKindCode(@Param("project") Project project, @Param("dependencyKindCode") String dependencyKindCode);
    
    @Query("SELECT COUNT(d) FROM ClassDependency d WHERE d.sourceClass.packageInfo = :packageInfo AND d.dependencyKind.code = :dependencyKindCode")
    long countByPackageInfoAndDependencyKindCode(@Param("packageInfo") PackageInfo packageInfo, @Param("dependencyKindCode") String dependencyKindCode);
}

