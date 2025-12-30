package com.agrimatch.notify.service;

import com.agrimatch.notify.dto.NotifyResponse;

import java.util.List;

public interface NotifyService {
    List<NotifyResponse> myList(Long userId);

    void markRead(Long userId, Long id);

    void markAllRead(Long userId);

    void send(Long toUserId, String type, String title, String content, String link);
}


