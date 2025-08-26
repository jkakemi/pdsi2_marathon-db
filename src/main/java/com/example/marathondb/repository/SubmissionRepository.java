package com.example.marathondb.repository;

import com.example.marathondb.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsBySourceSubmissionId(Long sourceSubmissionId);
    List<Submission> findByStudentId(Long studentId);
}