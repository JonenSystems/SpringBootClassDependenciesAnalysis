package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.Package;
import com.example.springbootprojectanalyser.model.entity.Project;
import java.util.List;
import java.util.Optional;

/**
 * パッケージリポジトリインターフェース
 */
public interface PackageRepository {

    /**
     * パッケージを保存する
     *
     * @param packageEntity パッケージエンティティ
     * @return 保存されたパッケージ
     */
    Package save(Package packageEntity);

    /**
     * プロジェクトに属するパッケージを検索する
     *
     * @param project プロジェクト
     * @return パッケージリスト
     */
    List<Package> findByProject(Project project);

    /**
     * フルネームでパッケージを検索する
     *
     * @param fullName フルネーム
     * @param project プロジェクト
     * @return パッケージ（存在しない場合は空）
     */
    Optional<Package> findByFullNameAndProject(String fullName, Project project);
}

