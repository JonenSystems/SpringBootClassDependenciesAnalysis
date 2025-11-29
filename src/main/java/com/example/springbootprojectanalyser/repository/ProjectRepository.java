package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.Project;
import java.util.Optional;

/**
 * プロジェクトリポジトリインターフェース
 */
public interface ProjectRepository {

    /**
     * プロジェクトを保存する
     *
     * @param project プロジェクトエンティティ
     * @return 保存されたプロジェクト
     */
    Project save(Project project);

    /**
     * ルートパスでプロジェクトを検索する
     *
     * @param rootPath ルートパス
     * @return プロジェクト（存在しない場合は空）
     */
    Optional<Project> findByRootPath(String rootPath);
}

