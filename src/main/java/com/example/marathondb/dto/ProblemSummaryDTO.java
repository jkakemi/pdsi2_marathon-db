package com.example.marathondb.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ProblemSummaryDTO {
    private String title;
    private String source;
    private String problemUrl;
    private Integer rating;
    private Set<String> tags;
}