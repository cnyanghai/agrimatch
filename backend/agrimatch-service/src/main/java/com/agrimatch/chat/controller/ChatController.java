package com.agrimatch.chat.controller;

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
}


