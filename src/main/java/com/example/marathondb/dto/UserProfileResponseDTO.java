package com.example.marathondb.dto;

import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private Long id;
    private String learningGoal;
    private String learningStyle;
    private String timeCommitment;
    private Integer currentPeriod;
}
