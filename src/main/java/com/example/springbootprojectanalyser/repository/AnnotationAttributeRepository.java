package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.AnnotationAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * アノテーション属性リポジトリ
 */
@Repository
public interface AnnotationAttributeRepository extends JpaRepository<AnnotationAttribute, Long> {
}




