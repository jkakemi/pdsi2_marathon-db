package com.example.marathondb.repository;

import com.example.marathondb.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Optional<Problem> findByContestIdAndProblemIndex(Integer contestId, String problemIndex);
}