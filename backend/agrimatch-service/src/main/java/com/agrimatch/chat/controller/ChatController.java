package com.agrimatch.chat.controller;

import com.agrimatch.chat.dto.ChatConversationOpenRequest;
import com.agrimatch.chat.dto.ChatConversationResponse;
import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.dto.ChatPeerResponse;
import com.agrimatch.chat.dto.ChatSendRequest;
import com.agrimatch.chat.service.ChatService;
import com.agrimatch.common.api.Result;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Validated
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/peers")
    public Result<List<ChatPeerResponse>> peers(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(chatService.peers(userId));
    }

    @GetMapping("/history")
    public Result<List<ChatMessageResponse>> history(Authentication authentication,
                                                     @RequestParam("peerUserId") @NotNull Long peerUserId,
                                                     @RequestParam(value = "limit", required = false) Integer limit) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(chatService.history(userId, peerUserId, limit));
    }

    @PostMapping("/send")
    public Result<Long> send(Authentication authentication, @Valid @RequestBody ChatSendRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(chatService.send(userId, req.getToUserId(), req.getContent()));
    }

    @PostMapping("/read")
    public Result<Void> read(Authentication authentication, @RequestParam("peerUserId") @NotNull Long peerUserId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        chatService.markRead(userId, peerUserId);
        return Result.success();
    }

    @PostMapping("/conversations/open")
    public Result<Long> openConversation(Authentication authentication, @Valid @RequestBody ChatConversationOpenRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        Long id = chatService.openConversation(userId, req.getPeerUserId(), req.getSubjectType(), req.getSubjectId(), req.getSubjectSnapshotJson());
        return Result.success(id);
    }

    @GetMapping("/conversations")
    public Result<List<ChatConversationResponse>> conversations(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(chatService.conversations(userId));
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<ChatMessageResponse>> conversationMessages(Authentication authentication,
                                                                  @PathVariable("id") Long conversationId,
                                                                  @RequestParam(value = "limit", required = false) Integer limit) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(chatService.conversationMessages(userId, conversationId, limit));
    }

    @PostMapping("/conversations/{id}/read")
    public Result<Void> readConversation(Authentication authentication, @PathVariable("id") Long conversationId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        chatService.markConversationRead(userId, conversationId);
        return Result.success();
    }

    @PostMapping("/messages/{id}/confirm")
    public Result<ChatMessageResponse> confirmOffer(Authentication authentication, @PathVariable("id") Long messageId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(chatService.confirmOffer(userId, messageId));
    }
}


