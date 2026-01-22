package com.agrimatch.user.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.dto.UserCreateRequest;
import com.agrimatch.user.dto.UserBriefResponse;
import com.agrimatch.user.dto.UserResponse;
import com.agrimatch.user.dto.UserRoleUpdateRequest;
import com.agrimatch.user.dto.UserUpdateRequest;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long create(UserCreateRequest req) {
        SysUser u = new SysUser();
        u.setUserName(req.getUserName());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setNickName(req.getNickName());
        u.setPhonenumber(req.getPhonenumber());
        u.setWechat(req.getWechat());
        u.setCompanyId(req.getCompanyId());
        u.setIsBuyer(normalize01(req.getIsBuyer()));
        u.setIsSeller(normalize01(req.getIsSeller()));
        u.setUserType(resolveUserType(u.getIsBuyer(), u.getIsSeller()));
        u.setPayInfoJson(req.getPayInfoJson());

        int rows = userMapper.insert(u);
        if (rows != 1 || u.getUserId() == null) {
            throw new ApiException(ResultCode.SERVER_ERROR);
        }
        return u.getUserId();
    }

    @Override
    public UserResponse getById(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        return toResponse(u);
    }

    @Override
    public void update(Long userId, UserUpdateRequest req) {
        SysUser u = new SysUser();
        u.setUserId(userId);
        u.setNickName(emptyToNull(req.getNickName()));
        u.setPhonenumber(emptyToNull(req.getPhonenumber()));
        u.setWechat(emptyToNull(req.getWechat()));
        u.setPosition(emptyToNull(req.getPosition()));
        u.setBirthDate(parseBirthDate(req.getBirthDate()));
        u.setGender(req.getGender());
        u.setBio(emptyToNull(req.getBio()));
        u.setAvatar(emptyToNull(req.getAvatar()));
        u.setCompanyId(req.getCompanyId());
        u.setPayInfoJson(emptyToNull(req.getPayInfoJson()));

        int rows = userMapper.update(u);
        if (rows != 1) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
    }

    @Override
    public void updateRoles(Long userId, UserRoleUpdateRequest req) {
        Integer isBuyer = normalize01(req.getIsBuyer());
        Integer isSeller = normalize01(req.getIsSeller());
        String userType = resolveUserType(isBuyer, isSeller);

        int rows = userMapper.updateRoles(userId, isBuyer, isSeller, userType);
        if (rows != 1) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
    }

    @Override
    public List<UserBriefResponse> search(String keyword, Integer limit) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        if (kw == null) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        int lim = (limit == null ? 20 : Math.max(1, Math.min(limit, 50)));
        return userMapper.search(kw, lim);
    }

    private UserResponse toResponse(SysUser u) {
        UserResponse r = new UserResponse();
        r.setUserId(u.getUserId());
        r.setUserName(u.getUserName());
        r.setNickName(u.getNickName());
        r.setPhonenumber(u.getPhonenumber());
        r.setWechat(u.getWechat());
        r.setCompanyId(u.getCompanyId());
        r.setIsBuyer(u.getIsBuyer());
        r.setIsSeller(u.getIsSeller());
        r.setUserType(u.getUserType());
        r.setPosition(u.getPosition());
        r.setBirthDate(u.getBirthDate() != null ? u.getBirthDate().toString() : null);
        r.setGender(u.getGender());
        r.setBio(u.getBio());
        r.setAvatar(u.getAvatar());
        r.setPayInfoJson(u.getPayInfoJson());
        r.setCreateTime(u.getCreateTime());
        r.setUpdateTime(u.getUpdateTime());
        return r;
    }

    private static Integer normalize01(Integer v) {
        if (v == null) return 0;
        return v != 0 ? 1 : 0;
    }

    private static String resolveUserType(Integer isBuyer, Integer isSeller) {
        int b = normalize01(isBuyer);
        int s = normalize01(isSeller);
        if (b == 1 && s == 1) return "CG_GY_USER";
        if (b == 1) return "CG_USER";
        if (s == 1) return "GY_USER";
        return "SYS_USER";
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }

    private static LocalDate parseBirthDate(String birthDateStr) {
        if (!StringUtils.hasText(birthDateStr)) {
            return null;
        }
        try {
            // 支持 YYYY-MM-DD 格式
            return LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            // 如果解析失败，返回 null（不阻塞保存）
            return null;
        }
    }
}


