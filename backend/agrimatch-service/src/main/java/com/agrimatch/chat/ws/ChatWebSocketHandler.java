package com.agrimatch.chat.ws;

import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.event.OfferUpdatedEvent;
import com.agrimatch.chat.service.ChatService;
import com.agrimatch.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
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
        Long userId = authenticate(session);
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
        String type = root.path("type").asText("");
        if (!StringUtils.hasText(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"missing type\"}"));
            return;
        }
        if ("PING".equalsIgnoreCase(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"PONG\",\"serverTime\":\"" + LocalDateTime.now() + "\"}"));
            return;
        }
        if (!"SEND".equalsIgnoreCase(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"unsupported type\"}"));
            return;
        }

        Long conversationId = root.hasNonNull("conversationId") ? root.get("conversationId").asLong() : null;
        String msgType = root.path("msgType").asText("TEXT");
        String content = root.path("content").asText("");
        String payloadJson = root.hasNonNull("payload") ? root.get("payload").toString() : null;
        String tempId = root.path("tempId").asText(null);

        if (conversationId == null) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"missing conversationId\"}"));
            return;
        }

        var saved = chatService.sendToConversation(fromUserId, conversationId, msgType, content, payloadJson);

        // 推送给接收方（在线的话）
        WebSocketSession toSession = sessions.get(saved.getToUserId());
        if (toSession != null && toSession.isOpen()) {
            String payload = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("type", "MESSAGE")
                    .put("conversationId", saved.getConversationId())
                    .set("message", objectMapper.valueToTree(saved))
            );
            toSession.sendMessage(new TextMessage(payload));
        }

        // 回执给发送方（包含 tempId -> id）
        var ack = objectMapper.createObjectNode()
                .put("type", "SENT")
                .put("conversationId", saved.getConversationId())
                .put("id", saved.getId());
        if (StringUtils.hasText(tempId)) ack.put("tempId", tempId);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        Object uidObj = session.getAttributes().get("userId");
        if (uidObj instanceof Long) {
            sessions.remove((Long) uidObj);
        }
    }

    @EventListener
    public void onOfferUpdated(OfferUpdatedEvent event) {
        broadcastOfferUpdate(event.getConversationId(), event.getAUserId(), event.getBUserId(), event.getMessage());
    }

    public void broadcastOfferUpdate(Long conversationId, Long aUserId, Long bUserId, ChatMessageResponse updatedMessage) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("type", "OFFER_UPDATED")
                    .put("conversationId", conversationId)
                    .set("message", objectMapper.valueToTree(updatedMessage))
            );
        } catch (Exception e) {
            return;
        }

        TextMessage textMessage = new TextMessage(payload);
        sendToUser(aUserId, textMessage);
        sendToUser(bUserId, textMessage);
    }

    private void sendToUser(Long userId, TextMessage message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (Exception ignore) {
            }
        }
    }

    private Long authenticate(WebSocketSession session) {
        // 1) Cookie: agrimatch_token=...
        String cookieToken = resolveTokenFromCookieHeader(session);

        // 2) 兼容旧方案：?token=xxx
        String queryToken = resolveTokenFromQuery(session.getUri());

        // 优先 cookie；若 cookie 存在但解析失败，fallback 到 query token（便于兜底）
        if (StringUtils.hasText(cookieToken)) {
            try {
                Claims claims = jwtTokenUtil.parseClaims(cookieToken);
                return Long.parseLong(claims.getSubject());
            } catch (Exception ignore) {
                // fall through
            }
        }

        if (StringUtils.hasText(queryToken)) {
            try {
                Claims claims = jwtTokenUtil.parseClaims(queryToken);
                return Long.parseLong(claims.getSubject());
            } catch (Exception ignore) {
                return null;
            }
        }

        return null;
    }

    private String resolveTokenFromCookieHeader(WebSocketSession session) {
        List<String> cookies = session.getHandshakeHeaders().get("Cookie");
        if (cookies == null || cookies.isEmpty()) return null;
        for (String cookieHeader : cookies) {
            if (!StringUtils.hasText(cookieHeader)) continue;
            String token = parseCookie(cookieHeader, "agrimatch_token");
            if (StringUtils.hasText(token)) return token;
        }
        return null;
    }

    private String resolveTokenFromQuery(URI uri) {
        if (uri == null) return null;
        String q = uri.getQuery();
        if (!StringUtils.hasText(q)) return null;
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
            return java.net.URLDecoder.decode(token, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ignore) {
            return token;
        }
    }

    private static String parseCookie(String cookieHeader, String name) {
        // cookieHeader example: "a=1; agrimatch_token=xxx; b=2"
        for (String part : cookieHeader.split(";")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            int idx = p.indexOf('=');
            if (idx <= 0) continue;
            String k = p.substring(0, idx).trim();
            String v = p.substring(idx + 1).trim();
            if (name.equals(k) && StringUtils.hasText(v)) return v;
        }
        return null;
    }
}


