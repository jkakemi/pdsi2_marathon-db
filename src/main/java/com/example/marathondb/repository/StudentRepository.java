package com.example.marathondb.repository;

import com.example.marathondb.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // JpaRepository<Student, Long>
    // 1. Student: É a entidade que este repositório vai gerenciar.
    // 2. Long: É o tipo da chave primária (o @Id) da entidade Student.

    Optional<Student> findByEmail(String email);

    Optional<Student> findByUsername(String username);

    List<Student> findByHandlesContainingIgnoreCase(String platformHandle);
}