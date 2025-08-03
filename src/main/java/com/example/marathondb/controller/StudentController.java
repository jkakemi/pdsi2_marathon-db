package com.example.marathondb.controller;

import com.example.marathondb.domain.Student;
import com.example.marathondb.dto.StudentRegistrationDTO;
import com.example.marathondb.dto.StudentResponseDTO;
import com.example.marathondb.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponseDTO> addStudent(@RequestBody StudentRegistrationDTO dto) {
        StudentResponseDTO newStudentDTO = studentService.registerNewStudent(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(newStudentDTO);

    }
}
