package com.agrimatch.user.mapper;

import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.dto.UserBriefResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int insert(SysUser user);

    SysUser selectById(@Param("userId") Long userId);

    SysUser selectByUserName(@Param("userName") String userName);

    int update(SysUser user);

    int updateRoles(@Param("userId") Long userId,
                    @Param("isBuyer") Integer isBuyer,
                    @Param("isSeller") Integer isSeller,
                    @Param("userType") String userType);

    List<UserBriefResponse> search(@Param("keyword") String keyword, @Param("limit") Integer limit);
}


