package com.example.marathondb.service;

import com.example.marathondb.domain.Student;
import com.example.marathondb.domain.Submission;
import com.example.marathondb.domain.UserProfile;
import com.example.marathondb.dto.*;
import com.example.marathondb.repository.StudentRepository;
import com.example.marathondb.repository.SubmissionRepository;
import com.example.marathondb.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final UserProfileRepository userProfileRepository;
    private final SubmissionRepository submissionRepository;

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
        newStudent.setPassword(passwordEncoder.encode(dto.getPassword()));
        newStudent.setHandles(dto.getHandles());

        UserProfile newProfile = new UserProfile();
        newProfile.setStudent(newStudent);
        newStudent.setUserProfile(newProfile);

        Student savedStudent = studentRepository.save(newStudent);

        return mapToStudentResponseDTO(savedStudent);
    }

    @Transactional
    public UserProfileResponseDTO updateUserProfile(String userEmail, UserProfileUpdateDTO dto) {
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        UserProfile profile = student.getUserProfile();
        profile.setLearningGoal(dto.getLearningGoal());
        profile.setLearningStyle(dto.getLearningStyle());
        profile.setTimeCommitment(dto.getTimeCommitment());
        profile.setCurrentPeriod(dto.getCurrentPeriod());

        userProfileRepository.save(profile);

        UserProfile savedProfile = userProfileRepository.save(profile);

        return mapToUserProfileResponseDTO(savedProfile);
    }

    private UserProfileResponseDTO mapToUserProfileResponseDTO(UserProfile profile) {
        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setId(profile.getId());
        dto.setLearningGoal(profile.getLearningGoal());
        dto.setLearningStyle(profile.getLearningStyle());
        dto.setTimeCommitment(profile.getTimeCommitment());
        dto.setCurrentPeriod(profile.getCurrentPeriod());
        return dto;
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

    public List<SubmissionResponseDTO> getSubmissionsForStudent(String userEmail) {
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        List<Submission> submissions = submissionRepository.findByStudentId(student.getId());

        return submissions.stream()
                .map(this::mapToSubmissionResponseDTO)
                .collect(Collectors.toList());
    }

    public UserStatisticsDTO getStatisticsForStudent(String userEmail) {
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        List<Submission> submissions = submissionRepository.findByStudentId(student.getId());

        if (submissions.isEmpty()) {
            return UserStatisticsDTO.builder().build();
        }

        long solvedCount = submissions.stream().filter(s -> "OK".equals(s.getVerdict())).count();

        Map<String, Long> verdictCounts = submissions.stream()
                .collect(Collectors.groupingBy(Submission::getVerdict, Collectors.counting()));

        String mostUsedLang = submissions.stream()
                .collect(Collectors.groupingBy(Submission::getLanguage, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");

        return UserStatisticsDTO.builder()
                .totalSubmissions(submissions.size())
                .solvedProblems(solvedCount)
                .verdicts(verdictCounts)
                .mostUsedLanguage(mostUsedLang)
                .build();
    }

    private SubmissionResponseDTO mapToSubmissionResponseDTO(Submission submission) {
        SubmissionResponseDTO dto = new SubmissionResponseDTO();
        dto.setId(submission.getId());
        dto.setVerdict(submission.getVerdict());
        dto.setLanguage(submission.getLanguage());
        dto.setSubmissionTime(submission.getSubmissionTime());

        ProblemSummaryDTO problemDto = new ProblemSummaryDTO();
        problemDto.setTitle(submission.getProblem().getTitle());
        problemDto.setSource(submission.getProblem().getSource());
        problemDto.setProblemUrl(submission.getProblem().getProblemUrl());

        dto.setProblem(problemDto);
        return dto;
    }
}
