package com.agrimatch.security;

public class LoginUser {
    private final Long userId;
    private final String userName;

    public LoginUser(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
}


