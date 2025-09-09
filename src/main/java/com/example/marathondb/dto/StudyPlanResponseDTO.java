package com.example.marathondb.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyPlanResponseDTO {
    private Long id;
    private String title;
    private String content;
    private boolean isActive;
    private LocalDateTime createdAt;
}