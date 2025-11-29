package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.Class;
import com.example.springbootprojectanalyser.model.entity.Package;
import java.util.List;
import java.util.Optional;

/**
 * クラスリポジトリインターフェース
 */
public interface ClassRepository {

    /**
     * クラスを保存する
     *
     * @param classEntity クラスエンティティ
     * @return 保存されたクラス
     */
    Class save(Class classEntity);

    /**
     * パッケージに属するクラスを検索する
     *
     * @param packageEntity パッケージ
     * @return クラスリスト
     */
    List<Class> findByPackage(Package packageEntity);

    /**
     * 完全修飾名でクラスを検索する
     *
     * @param fullQualifiedName 完全修飾名
     * @return クラス（存在しない場合は空）
     */
    Optional<Class> findByFullQualifiedName(String fullQualifiedName);
}

