package com.example.marathondb.dto;

import lombok.Data;

@Data
public class StudentUpdateDTO {
    private String username;
    private String email;
    private String handles;
    private String password;
}