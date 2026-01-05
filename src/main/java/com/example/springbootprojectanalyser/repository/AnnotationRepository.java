package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * アノテーションリポジトリ
 */
@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByMember_Id(Long memberId);
    List<Annotation> findByClassEntity_Id(Long classId);
}

