package com.example.marathondb.service;

import com.example.marathondb.domain.Student;
import com.example.marathondb.domain.UserProfile;
import com.example.marathondb.dto.StudentRegistrationDTO;
import com.example.marathondb.dto.StudentResponseDTO;
import com.example.marathondb.dto.UserProfileResponseDTO;
import com.example.marathondb.repository.StudentRepository;
import com.example.marathondb.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Transactional
    public StudentResponseDTO registerNewStudent(StudentRegistrationDTO dto) {
        studentRepository.findByEmail(dto.getEmail()).ifPresent(s -> {
            throw new IllegalStateException("Email já cadastrado.");
        });

        studentRepository.findByUsername(dto.getUsername()).ifPresent(s -> {
            throw new IllegalStateException("Username já em uso.");
        });

        Student newStudent = new Student();
        newStudent.setEmail(dto.getEmail());
        newStudent.setUsername(dto.getUsername());
        newStudent.setHandles(dto.getHandles());

        UserProfile newProfile = new UserProfile();
        newProfile.setStudent(newStudent);
        newStudent.setUserProfile(newProfile);

        Student savedStudent = studentRepository.save(newStudent);

        return mapToStudentResponseDTO(savedStudent);
    }

    private StudentResponseDTO mapToStudentResponseDTO(Student student) {
        StudentResponseDTO responseDTO = new StudentResponseDTO();
        responseDTO.setId(student.getId());
        responseDTO.setEmail(student.getEmail());
        responseDTO.setUsername(student.getUsername());
        responseDTO.setHandles(student.getHandles());
        responseDTO.setCreatedAt(student.getCreatedAt());

        if(student.getUserProfile() != null) {
            UserProfileResponseDTO profileDTO = new UserProfileResponseDTO();
            profileDTO.setId(student.getUserProfile().getId());
            profileDTO.setLearningGoal(student.getUserProfile().getLearningGoal());
            profileDTO.setLearningStyle(student.getUserProfile().getLearningStyle());
            profileDTO.setTimeCommitment(student.getUserProfile().getTimeCommitment());
            profileDTO.setCurrentPeriod(student.getUserProfile().getCurrentPeriod());
            responseDTO.setUserProfile(profileDTO);
        }
        return responseDTO;

    }
}
