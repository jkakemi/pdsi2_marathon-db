package com.example.marathondb.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class UserStatisticsDTO {
    private long totalSubmissions;
    private long solvedProblems;
    private String mostUsedLanguage;
    private Map<String, Long> verdicts; // "OK": 50 | "WRONG_ANSWER": 30
}