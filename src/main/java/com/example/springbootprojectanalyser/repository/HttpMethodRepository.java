package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.HttpMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * HTTPメソッドリポジトリ
 */
@Repository
public interface HttpMethodRepository extends JpaRepository<HttpMethod, Long> {
    Optional<HttpMethod> findByMethodName(String methodName);
}

