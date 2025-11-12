package com.example.marathondb.controller;

import com.example.marathondb.domain.Student;
import com.example.marathondb.dto.*;
import com.example.marathondb.repository.StudentRepository;
import com.example.marathondb.service.CodeforcesService;
import com.example.marathondb.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final CodeforcesService codeforcesService;

    @PostMapping
    public ResponseEntity<StudentResponseDTO> addStudent(@RequestBody StudentRegistrationDTO dto) {
        StudentResponseDTO newStudentDTO = studentService.registerNewStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newStudentDTO);
    }

    @PostMapping("/me/sync-codeforces")
    public ResponseEntity<String> syncCodeforcesSubmissions(Authentication authentication) {
        String userEmail = authentication.getName();
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));
        String codeforcesHandle = student.getHandles();
        if (codeforcesHandle == null || codeforcesHandle.isBlank()) {
            return ResponseEntity.badRequest().body("Handle do Codeforces não foi adicionado para este estudante.");
        }
        codeforcesService.syncSubmissionsForStudent(student, codeforcesHandle);
        return ResponseEntity.ok("Sincronização com o Codeforces iniciada com sucesso!");
    }

    @GetMapping("/me/submissions")
    public ResponseEntity<List<SubmissionResponseDTO>> getMySubmissions(Authentication authentication) {
        String userEmail = authentication.getName();
        List<SubmissionResponseDTO> submissions = studentService.getSubmissionsForStudent(userEmail);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/me/statistics")
    public ResponseEntity<UserStatisticsDTO> getMyStatistics(Authentication authentication) {
        String userEmail = authentication.getName();
        UserStatisticsDTO statistics = studentService.getStatisticsForStudent(userEmail);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(Authentication authentication) {
        System.out.println("[DEBUG] Reached getMyProfile method in StudentController.");
        String userEmail = authentication.getName();
        UserProfileResponseDTO profile = studentService.getUserProfile(userEmail);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileResponseDTO> updateUserProfile(Authentication authentication, @RequestBody UserProfileUpdateDTO dto) {
        String userEmail = authentication.getName();
        UserProfileResponseDTO updatedProfile = studentService.updateUserProfile(userEmail, dto);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/me")
    public ResponseEntity<StudentResponseDTO> getMyDetails(Authentication authentication) {
        String userEmail = authentication.getName();
        StudentResponseDTO studentDetails = studentService.getStudentDetails(userEmail);
        return ResponseEntity.ok(studentDetails);
    }

    @PutMapping("/me")
    public ResponseEntity<StudentResponseDTO> updateMyDetails(Authentication authentication, @RequestBody StudentUpdateDTO dto) {
        String userEmail = authentication.getName();
        try {
            StudentResponseDTO updatedStudent = studentService.updateStudent(userEmail, dto);
            return ResponseEntity.ok(updatedStudent);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
