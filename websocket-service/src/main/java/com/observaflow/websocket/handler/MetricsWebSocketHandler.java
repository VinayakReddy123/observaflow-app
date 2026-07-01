package com.observaflow.websocket.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.observaflow.websocket.registry.SessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsWebSocketHandler implements WebSocketHandler{
    
    private final SessionRegistry registry;

    @Override
    public Mono<Void> handle(WebSocketSession session){
        String tenantId = extractTenantId(session);
        if(tenantId == null){
            log.warn("WebSocket connection rejected — missing tenantId");
            return session.close();
        }

        registry.register(tenantId, session);
        log.info("Session registered | tenantId={} sessionId={}", tenantId, session.getId());

        return session.receive()
                .doFinally(signal -> {
                    registry.remove(tenantId, session);
                    log.info("Session removed | tenantId={} sessionId={} signal={}",
                            tenantId, session.getId(), signal);
                })
                .then();

    }

    private String extractTenantId(WebSocketSession session){
        String query = session.getHandshakeInfo().getUri().getQuery();
        if(query!=null && query.contains("tenantId=")){
            for(String param : query.split("&")){
                if(param.startsWith("tenantId=")){
                    return param.substring("tenantId=".length());
                }
            }
        }
        return null;
    }
}
