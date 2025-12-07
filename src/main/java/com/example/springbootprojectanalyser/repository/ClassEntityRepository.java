package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.ClassEntity;
import com.example.springbootprojectanalyser.model.entity.PackageInfo;
import com.example.springbootprojectanalyser.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * クラスエンティティリポジトリインターフェース
 */
@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByProject(Project project);
    
    List<ClassEntity> findByPackageInfo(PackageInfo packageInfo);
    
    @Query("SELECT c FROM ClassEntity c WHERE c.project = :project AND c.fullQualifiedName = :fullQualifiedName")
    Optional<ClassEntity> findByProjectAndFullQualifiedName(@Param("project") Project project, @Param("fullQualifiedName") String fullQualifiedName);
}

