package com.agrimatch.notify.mapper;

import com.agrimatch.notify.domain.BusNotify;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotifyMapper {
    int insert(BusNotify n);

    List<BusNotify> selectMyList(@Param("toUserId") Long toUserId);

    int markRead(@Param("toUserId") Long toUserId, @Param("id") Long id);

    int markAllRead(@Param("toUserId") Long toUserId);
}


