package com.example.marathondb.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String handles;
    private LocalDateTime createdAt;
    private UserProfileResponseDTO userProfile;
}
