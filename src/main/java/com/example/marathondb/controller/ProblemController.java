package com.example.marathondb.controller;

import com.example.marathondb.dto.ProblemSummaryDTO;
import com.example.marathondb.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProblemSummaryDTO>> getRecommendations(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ProblemSummaryDTO> recommendations = recommendationService.recommendProblems(userEmail);
        return ResponseEntity.ok(recommendations);
    }
}