package com.example.zahaawiiblog.logginFeature.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Logging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String username;

    private String description;

    /*
    Action ID is what action that has occurred.
    ID 1: User created something
    ID 2: User tried to create but failed
    ID 3: User deleted something
    ID 4: User tried to delete but failed
    ID 5: User accessed homepage
    ID 6: User accessed a profile
    ID 7: User logged in
     */
    private Long actionId;

    private LocalDateTime localDateTime;
}
