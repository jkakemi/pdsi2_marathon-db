package com.example.marathondb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeforcesSubmissionDTO {
    private Long id;
    private CodeforcesProblemDTO problem;
    private String programmingLanguage;
    private String verdict;
    private long creationTimeSeconds;
}