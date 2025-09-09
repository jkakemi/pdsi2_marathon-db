package com.example.marathondb.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "topics")
    @ToString.Exclude
    private Set<Problem> problems;
}
