package com.linguaflow.linguaflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AdminSessionService {

    private final Duration idleTimeout;
    private final ConcurrentMap<String, Instant> lastActivityByUser = new ConcurrentHashMap<>();

    public AdminSessionService(
            @Value("${app.admin.idle-timeout-ms:1800000}") long idleTimeoutMs) {
        if (idleTimeoutMs <= 0) {
            throw new IllegalArgumentException("Admin idle timeout must be greater than zero");
        }
        idleTimeout = Duration.ofMillis(idleTimeoutMs);
    }

    public void start(String username) {
        lastActivityByUser.put(username, Instant.now());
    }

    public boolean touchIfActive(String username) {
        Instant lastActivity = lastActivityByUser.get(username);
        if (lastActivity == null || lastActivity.plus(idleTimeout).isBefore(Instant.now())) {
            lastActivityByUser.remove(username, lastActivity);
            return false;
        }
        lastActivityByUser.put(username, Instant.now());
        return true;
    }

    public void clear(String username) {
        lastActivityByUser.remove(username);
    }
}
