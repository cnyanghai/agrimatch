package com.agrimatch.user.mapper;

import com.agrimatch.user.domain.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysLoginLogMapper {
    /**
     * 新增登录日志
     */
    int insert(SysLoginLog loginLog);

    /**
     * 查询用户最近登录日志
     */
    List<SysLoginLog> selectByUserName(@Param("userName") String userName, @Param("limit") int limit);
}

