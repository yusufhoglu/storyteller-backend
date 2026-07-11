package com.server.aydede.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voice_session")
@Getter
@Setter
@NoArgsConstructor
public class VoiceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "room_name", nullable = false)
    @NotBlank
    private String roomName;

    @Column(name = "agent_name", nullable = false)
    @NotBlank
    private String agentName;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "metadata_json", nullable = true)
    private String metadataJson;

    @Column(name = "started_at", nullable = true)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = true)
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds", nullable = true)
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
