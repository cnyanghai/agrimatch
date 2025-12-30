package com.agrimatch.user.service;

import com.agrimatch.user.dto.UserCreateRequest;
import com.agrimatch.user.dto.UserBriefResponse;
import com.agrimatch.user.dto.UserResponse;
import com.agrimatch.user.dto.UserRoleUpdateRequest;
import com.agrimatch.user.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    Long create(UserCreateRequest req);

    UserResponse getById(Long userId);

    void update(Long userId, UserUpdateRequest req);

    void updateRoles(Long userId, UserRoleUpdateRequest req);

    List<UserBriefResponse> search(String keyword, Integer limit);
}


