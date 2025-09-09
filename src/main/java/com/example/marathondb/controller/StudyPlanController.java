package com.example.marathondb.controller;

import com.example.marathondb.dto.StudyPlanResponseDTO;
import com.example.marathondb.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final RecommendationService recommendationService;

    @PostMapping("/generate")
    public ResponseEntity<StudyPlanResponseDTO> generateNewStudyPlan(Authentication authentication) {
        String userEmail = authentication.getName();
        StudyPlanResponseDTO newPlanDto = recommendationService.generateStudyPlan(userEmail);
        return ResponseEntity.ok(newPlanDto);
    }
}