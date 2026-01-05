package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * メンバータイプリポジトリ
 */
@Repository
public interface MemberTypeRepository extends JpaRepository<MemberType, String> {
    Optional<MemberType> findByCode(String code);
}

