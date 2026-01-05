package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * クラスタイプリポジトリ
 */
@Repository
public interface ClassTypeRepository extends JpaRepository<ClassType, String> {
    Optional<ClassType> findByCode(String code);
}

