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
}


