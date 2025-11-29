package com.example.springbootprojectanalyser.repository.impl;

import com.example.springbootprojectanalyser.model.entity.Project;
import com.example.springbootprojectanalyser.repository.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * プロジェクトリポジトリ実装クラス
 */
@Repository
public interface ProjectRepositoryImpl extends JpaRepository<Project, Long>, ProjectRepository {

    /**
     * ルートパスでプロジェクトを検索する
     *
     * @param rootPath ルートパス
     * @return プロジェクト（存在しない場合は空）
     */
    java.util.Optional<Project> findByRootPath(String rootPath);
}

