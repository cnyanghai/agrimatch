package com.agrimatch.notify.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.notify.domain.BusNotify;
import com.agrimatch.notify.dto.NotifyResponse;
import com.agrimatch.notify.mapper.NotifyMapper;
import com.agrimatch.notify.service.NotifyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotifyServiceImpl implements NotifyService {
    private final NotifyMapper notifyMapper;

    public NotifyServiceImpl(NotifyMapper notifyMapper) {
        this.notifyMapper = notifyMapper;
    }

    @Override
    public List<NotifyResponse> myList(Long userId) {
        if (userId == null) throw new ApiException(401, "未登录");
        List<BusNotify> list = notifyMapper.selectMyList(userId);
        List<NotifyResponse> out = new ArrayList<>();
        for (BusNotify n : list) {
            NotifyResponse r = new NotifyResponse();
            r.setId(n.getId());
            r.setType(n.getType());
            r.setTitle(n.getTitle());
            r.setContent(n.getContent());
            r.setLink(n.getLink());
            r.setRead(n.getIsRead() != null && n.getIsRead() == 1);
            r.setCreateTime(n.getCreateTime());
            out.add(r);
        }
        return out;
    }

    @Override
    public void markRead(Long userId, Long id) {
        if (userId == null || id == null) throw new ApiException(ResultCode.PARAM_ERROR);
        int rows = notifyMapper.markRead(userId, id);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
    }

    @Override
    public void markAllRead(Long userId) {
        if (userId == null) throw new ApiException(401, "未登录");
        notifyMapper.markAllRead(userId);
    }

    @Override
    public void send(Long toUserId, String type, String title, String content, String link) {
        if (toUserId == null || !StringUtils.hasText(type) || !StringUtils.hasText(title)) return;
        BusNotify n = new BusNotify();
        n.setToUserId(toUserId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(StringUtils.hasText(content) ? content : null);
        n.setLink(StringUtils.hasText(link) ? link : null);
        notifyMapper.insert(n);
    }
}


