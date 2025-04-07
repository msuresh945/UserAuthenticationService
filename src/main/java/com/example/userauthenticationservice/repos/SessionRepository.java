package com.example.userauthenticationservice.repos;

import com.example.userauthenticationservice.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByUserEmail(String email);
    Session save(Session session);
    void deleteByUserEmail(String email);
}
