package com.agrimatch.auth.controller;

import com.agrimatch.auth.dto.LoginRequest;
import com.agrimatch.auth.dto.LoginBySmsRequest;
import com.agrimatch.auth.dto.LoginResponse;
import com.agrimatch.auth.dto.MeResponse;
import com.agrimatch.auth.dto.RegisterRequest;
import com.agrimatch.auth.dto.SmsSendRequest;
import com.agrimatch.auth.service.AuthService;
import com.agrimatch.auth.service.SmsCodeService;
import com.agrimatch.common.api.Result;
import com.agrimatch.security.LoginUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SmsCodeService smsCodeService;

    public AuthController(AuthService authService, SmsCodeService smsCodeService) {
        this.authService = authService;
        this.smsCodeService = smsCodeService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req.getUserName(), req.getPassword()));
    }

    @PostMapping("/login/sms")
    public Result<LoginResponse> loginBySms(@Valid @RequestBody LoginBySmsRequest req) {
        // type=2 登录
        smsCodeService.verifyOrThrow(req.getPhone(), 2, req.getSmsCode());
        return Result.success(authService.loginByPhone(req.getPhone()));
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSms(@Valid @RequestBody SmsSendRequest req) {
        smsCodeService.send(req.getPhone(), req.getType());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(authService.register(req));
    }

    @GetMapping("/me")
    public Result<MeResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return Result.fail(401, "未登录");
        }
        LoginUser p = (LoginUser) authentication.getPrincipal();
        return Result.success(authService.me(p.getUserId()));
    }
}


