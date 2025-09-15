package com.example.marathondb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemIngestionDTO {
    private String problemId;
    private String name;
    private String problemPageUrl;
    private String difficulty;
    private int estimatedDacu;
}