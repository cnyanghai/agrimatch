package com.agrimatch.util;

import com.agrimatch.common.exception.ApiException;
import com.agrimatch.security.LoginUser;
import org.springframework.security.core.Authentication;

public class SecurityUtil {
    private SecurityUtil() {
    }

    public static Long requireUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new ApiException(401, "未登录");
        }
        return ((LoginUser) authentication.getPrincipal()).getUserId();
    }

    /**
     * 可选获取用户ID，如果未登录返回 null
     */
    public static Long getUserIdOrNull(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return null;
        }
        return ((LoginUser) authentication.getPrincipal()).getUserId();
    }
}


