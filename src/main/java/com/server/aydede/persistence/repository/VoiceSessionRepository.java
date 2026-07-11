package com.server.aydede.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import com.server.aydede.persistence.entity.VoiceSession;
import com.server.aydede.persistence.entity.User;

public interface VoiceSessionRepository extends JpaRepository<VoiceSession, UUID> {
    Optional<VoiceSession> findByRoomName(String roomName);

    long countByUser(User user);
}
