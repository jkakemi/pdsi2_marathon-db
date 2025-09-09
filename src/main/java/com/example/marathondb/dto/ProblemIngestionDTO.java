package com.example.marathondb.dto;

import lombok.Data;

@Data
public class ProblemIngestionDTO {
    private String problemId;
    private String name;
    private String difficulty;
    private int estimatedDacu;
}