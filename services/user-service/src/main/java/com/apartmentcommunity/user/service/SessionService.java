package com.apartmentcommunity.user.service;

import com.apartmentcommunity.user.model.Session;
import com.apartmentcommunity.user.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    @Autowired
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public String createSession(Long userId) {
        String token = UUID.randomUUID().toString();
        Session session = new Session(token, userId);
        sessionRepository.save(session);
        return token;
    }

    public Optional<Session> getSessionByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public void deleteSession(String token) {
        sessionRepository.deleteByToken(token);
    }
}

