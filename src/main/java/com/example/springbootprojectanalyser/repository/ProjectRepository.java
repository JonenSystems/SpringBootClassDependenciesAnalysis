package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * プロジェクトリポジトリインターフェース
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByRootPath(String rootPath);
}

