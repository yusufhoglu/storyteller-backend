package com.server.aydede.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "firebase_uid", nullable = false)
    @NotBlank
    private String firebaseUid;

    @Column(name = "auth_provider", nullable = false)
    @NotBlank
    private String authProvider;

    @Column(name = "plan", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserPlan plan;

    @Column(name = "age", nullable = true)
    @Min(0)
    private Integer age;

    @Column(name = "gender", nullable = true)
    private String gender;

    @Column(name = "language", nullable = true)
    private String language = "tr";

    @Column(name = "fav_books", nullable = true)
    private String favBooks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
