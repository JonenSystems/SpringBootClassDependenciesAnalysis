package com.example.springbootprojectanalyser.repository;

import com.example.springbootprojectanalyser.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * メンバーリポジトリ
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByClassEntity_Id(Long classId);
    
    @Query("SELECT m FROM Member m WHERE m.classEntity.id = :classId AND m.memberType.code = :memberTypeCode")
    List<Member> findByClassIdAndMemberType(@Param("classId") Long classId, @Param("memberTypeCode") String memberTypeCode);
}

