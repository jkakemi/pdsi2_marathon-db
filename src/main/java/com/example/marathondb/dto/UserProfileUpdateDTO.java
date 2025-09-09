package com.example.marathondb.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String learningGoal;
    private String learningStyle;
    private String timeCommitment;
    private Integer currentPeriod;
}