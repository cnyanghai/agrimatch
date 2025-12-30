package com.agrimatch.auth.service;

import com.agrimatch.auth.dto.LoginResponse;
import com.agrimatch.auth.dto.MeResponse;
import com.agrimatch.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(String userName, String password);

    LoginResponse register(RegisterRequest req);

    MeResponse me(Long userId);
}


