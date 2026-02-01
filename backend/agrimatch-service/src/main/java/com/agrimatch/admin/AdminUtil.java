package com.agrimatch.admin;

import com.agrimatch.common.exception.ApiException;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.util.SecurityUtil;
import org.springframework.security.core.Authentication;

public class AdminUtil {
    private AdminUtil() {
    }

    public static Long requireAdmin(Authentication authentication, UserMapper userMapper) {
        Long userId = SecurityUtil.requireUserId(authentication);
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsAdmin() == null || user.getIsAdmin() != 1) {
            throw new ApiException(403, "无管理员权限");
        }
        return userId;
    }
}
