package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.DependencyKindEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 依存タイプリポジトリインターフェース
 */
@Repository
public interface DependencyKindRepository extends JpaRepository<DependencyKindEntity, String> {
    Optional<DependencyKindEntity> findByCode(String code);
}

