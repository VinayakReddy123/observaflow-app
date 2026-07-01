package com.observaflow.websocket.registry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class SessionRegistry {

    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(String tenantId, WebSocketSession session) {
        sessions.computeIfAbsent(tenantId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(String tenantId, WebSocketSession session) {
        Set<WebSocketSession> tenantSessions = sessions.get(tenantId);
        if (tenantSessions != null) {
            tenantSessions.remove(session);
            if (tenantSessions.isEmpty()) {
                sessions.remove(tenantId);
            }
        }
    }

    public Set<WebSocketSession> getSessions(String tenantId) {
        return sessions.getOrDefault(tenantId, Collections.emptySet());
    }
}
