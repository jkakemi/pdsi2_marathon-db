package com.example.marathondb.controller;

import com.example.marathondb.dto.StudyPlanResponseDTO;
import com.example.marathondb.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<StudyPlanResponseDTO>> getMyStudyPlans(Authentication authentication) {
        String userEmail = authentication.getName();
        List<StudyPlanResponseDTO> plans = recommendationService.getStudyPlansForUser(userEmail);
        return ResponseEntity.ok(plans);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable Long planId, Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            recommendationService.deleteStudyPlan(planId, userEmail);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}