package com.agrimatch.chat.ws;

import com.agrimatch.chat.service.ChatService;
import com.agrimatch.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final JwtTokenUtil jwtTokenUtil;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(JwtTokenUtil jwtTokenUtil, ChatService chatService, ObjectMapper objectMapper) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        Long userId = authenticate(session.getUri());
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("unauthorized"));
            return;
        }
        session.getAttributes().put("userId", userId);
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("{\"type\":\"CONNECTED\",\"serverTime\":\"" + LocalDateTime.now() + "\"}"));
    }

    @Override
    @SuppressWarnings("null")
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        Object uidObj = session.getAttributes().get("userId");
        if (!(uidObj instanceof Long)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("unauthorized"));
            return;
        }
        Long fromUserId = (Long) uidObj;
        JsonNode root = objectMapper.readTree(message.getPayload());
        Long toUserId = root.hasNonNull("toUserId") ? root.get("toUserId").asLong() : null;
        String content = root.path("content").asText("").trim();
        if (toUserId == null || content.isEmpty()) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"invalid payload\"}"));
            return;
        }

        Long msgId = chatService.send(fromUserId, toUserId, content);

        // 推送给接收方（在线的话）
        WebSocketSession toSession = sessions.get(toUserId);
        if (toSession != null && toSession.isOpen()) {
            String payload = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("type", "MESSAGE")
                    .put("id", msgId)
                    .put("fromUserId", fromUserId)
                    .put("toUserId", toUserId)
                    .put("content", content)
            );
            toSession.sendMessage(new TextMessage(payload));
        }

        // 回执给发送方
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("type", "SENT")
                .put("id", msgId)
                .put("toUserId", toUserId)
        )));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        Object uidObj = session.getAttributes().get("userId");
        if (uidObj instanceof Long) {
            sessions.remove((Long) uidObj);
        }
    }

    private Long authenticate(URI uri) {
        if (uri == null) return null;
        String q = uri.getQuery();
        if (!StringUtils.hasText(q)) return null;
        // token=xxx
        String token = null;
        for (String part : q.split("&")) {
            int idx = part.indexOf('=');
            if (idx <= 0) continue;
            String k = part.substring(0, idx);
            String v = part.substring(idx + 1);
            if ("token".equals(k)) token = v;
        }
        if (!StringUtils.hasText(token)) return null;
        try {
            // token 可能被 URL 编码过（%2E 等），这里尝试解码
            String t = java.net.URLDecoder.decode(token, java.nio.charset.StandardCharsets.UTF_8);
            Claims claims = jwtTokenUtil.parseClaims(t);
            return Long.parseLong(claims.getSubject());
        } catch (Exception ignore) {
            return null;
        }
    }
}


