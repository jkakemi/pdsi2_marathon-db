package com.example.marathondb.repository;

import com.example.marathondb.domain.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findByContestIdAndProblemIndex(Integer contestId, String problemIndex);

    boolean existsByProblemUrl(String problemUrl);

    @Query("SELECT p FROM Problem p WHERE p.source = :source AND p.topics IS EMPTY")
    List<Problem> findProblemsBySourceAndWithoutTopics(@Param("source") String source);

    @Query(value = "SELECT * FROM problems p WHERE p.source = :source AND p.id NOT IN " +
            "(SELECT s.problem_id FROM submissions s WHERE s.student_id = :studentId AND s.verdict = 'OK') " +
            "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Problem> findRandomUnsolvedProblemsBySource(
            @Param("studentId") Long studentId,
            @Param("source") String source,
            @Param("limit") int limit
    );

    @Query("SELECT p FROM Problem p JOIN p.topics t WHERE t.name IN :topicNames AND p.id NOT IN " +
            "(SELECT s.problem.id FROM Submission s WHERE s.student.id = :studentId AND s.verdict = 'OK')")
    List<Problem> findUnsolvedProblemsByTopics(
            @Param("studentId") Long studentId,
            @Param("topicNames") Set<String> topicNames,
            Pageable pageable
    );
}