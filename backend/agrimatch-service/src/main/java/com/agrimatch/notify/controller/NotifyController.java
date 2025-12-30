package com.agrimatch.notify.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.notify.dto.NotifyResponse;
import com.agrimatch.notify.service.NotifyService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notify")
@Validated
public class NotifyController {
    private final NotifyService notifyService;

    public NotifyController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @GetMapping("/my")
    public Result<List<NotifyResponse>> my(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(notifyService.myList(userId));
    }

    @PostMapping("/my/read/{id}")
    public Result<Void> read(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        notifyService.markRead(userId, id);
        return Result.success();
    }

    @PostMapping("/my/read-all")
    public Result<Void> readAll(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        notifyService.markAllRead(userId);
        return Result.success();
    }
}


