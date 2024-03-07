package com.vicarius.accesslimiting.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.vicarius.accesslimiting.utils.Constants.QUOTA_LIMIT;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    private String id = UUID.randomUUID().toString();
    private String firstName;
    private String lastName;
    private LocalDateTime lastLoginTimeUtc = LocalDateTime.now();
    private int quota = QUOTA_LIMIT;

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String id, String firstName, String lastName, int quota) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.quota = quota;
    }
}