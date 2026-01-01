package com.agrimatch.auth.service;

import com.agrimatch.auth.dto.LoginResponse;
import com.agrimatch.auth.dto.MeResponse;
import com.agrimatch.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(String userName, String password);

    /**
     * 验证码登录：校验验证码后直接按手机号/账号签发 token
     */
    LoginResponse loginByPhone(String phone);

    LoginResponse register(RegisterRequest req);

    MeResponse me(Long userId);
}


