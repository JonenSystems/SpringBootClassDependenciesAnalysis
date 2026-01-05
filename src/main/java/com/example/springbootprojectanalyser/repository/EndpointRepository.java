package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * エンドポイントリポジトリ
 */
@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, String> {
    List<Endpoint> findByClassEntity_Project_Id(Long projectId);
    
    @Query("SELECT e FROM Endpoint e WHERE e.classEntity.project.id = :projectId ORDER BY e.classEntity.fullQualifiedName, e.uri")
    List<Endpoint> findByProjectIdOrdered(@Param("projectId") Long projectId);
}

