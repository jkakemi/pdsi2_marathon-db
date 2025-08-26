package com.example.marathondb.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmissionResponseDTO {
    private Long id;
    private String verdict;
    private String language;
    private LocalDateTime submissionTime;
    private ProblemSummaryDTO problem;
}