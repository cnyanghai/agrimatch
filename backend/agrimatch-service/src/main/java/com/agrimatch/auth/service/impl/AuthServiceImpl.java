package com.agrimatch.auth.service.impl;

import com.agrimatch.auth.dto.LoginResponse;
import com.agrimatch.auth.dto.MeResponse;
import com.agrimatch.auth.dto.RegisterRequest;
import com.agrimatch.auth.service.AuthService;
import com.agrimatch.auth.service.SmsCodeService;
import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.security.JwtTokenUtil;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.domain.SysLoginLog;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.user.mapper.SysLoginLogMapper;
import com.agrimatch.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final CompanyMapper companyMapper;
    private final SmsCodeService smsCodeService;
    private final SysLoginLogMapper loginLogMapper;

    // 登录失败锁定：5次失败锁定15分钟
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MS = 15 * 60_000L;
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    private static class LoginAttempt {
        int failCount;
        long lockUntil;

        LoginAttempt() {
            this.failCount = 0;
            this.lockUntil = 0;
        }
    }

    public AuthServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenUtil jwtTokenUtil,
                           CompanyMapper companyMapper,
                           SmsCodeService smsCodeService,
                           SysLoginLogMapper loginLogMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.companyMapper = companyMapper;
        this.smsCodeService = smsCodeService;
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public LoginResponse login(String userName, String password) {
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        // 检查是否被锁定
        checkLockout(userName);

        SysUser u = userMapper.selectByUserName(userName);
        if (u == null || !StringUtils.hasText(u.getPassword())) {
            recordFailedAttempt(userName);
            recordLoginLog(userName, "1", "账号或密码错误");
            throw new ApiException(401, "账号或密码错误");
        }
        if (!passwordEncoder.matches(password, u.getPassword())) {
            recordFailedAttempt(userName);
            recordLoginLog(userName, "1", "账号或密码错误");
            throw new ApiException(401, "账号或密码错误");
        }
        // 登录成功，清除失败记录
        clearAttempts(userName);
        String token = jwtTokenUtil.generateToken(u.getUserId(), u.getUserName());
        recordLoginLog(userName, "0", "登录成功");
        return new LoginResponse(token);
    }

    @Override
    public LoginResponse loginByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        String userName = phone.trim();
        SysUser u = userMapper.selectByUserName(userName);

        if (u == null) {
            // 自动注册：短信验证通过即创建账号
            u = new SysUser();
            u.setUserName(userName);
            u.setNickName(userName); // 默认昵称为手机号，用户可后续修改
            u.setPhonenumber(userName);
            u.setUserType("SYS_USER");
            u.setIsBuyer(0);
            u.setIsSeller(0);
            int rows = userMapper.insert(u);
            if (rows != 1 || u.getUserId() == null) {
                throw new ApiException(ResultCode.SERVER_ERROR);
            }
            log.info("Auto-registered new user via SMS login: phone={}, userId={}", userName, u.getUserId());
            recordLoginLog(userName, "0", "短信登录（新用户自动注册）");
        } else {
            recordLoginLog(userName, "0", "短信登录成功");
        }

        String token = jwtTokenUtil.generateToken(u.getUserId(), u.getUserName());
        return new LoginResponse(token);
    }

    private void checkLockout(String userName) {
        LoginAttempt attempt = loginAttempts.get(userName);
        if (attempt != null && attempt.lockUntil > System.currentTimeMillis()) {
            long remainMin = (attempt.lockUntil - System.currentTimeMillis()) / 60_000 + 1;
            throw new ApiException(429, "登录失败次数过多，请" + remainMin + "分钟后再试");
        }
    }

    private void recordFailedAttempt(String userName) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(userName, k -> new LoginAttempt());
        attempt.failCount++;
        if (attempt.failCount >= MAX_ATTEMPTS) {
            attempt.lockUntil = System.currentTimeMillis() + LOCKOUT_MS;
            attempt.failCount = 0;
        }
    }

    private void clearAttempts(String userName) {
        loginAttempts.remove(userName);
    }

    @Override
    public void resetPassword(String phone, String newPassword) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(newPassword)) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        SysUser u = userMapper.selectByUserName(phone.trim());
        if (u == null) {
            throw new ApiException(404, "账号不存在");
        }
        SysUser patch = new SysUser();
        patch.setUserId(u.getUserId());
        patch.setPassword(passwordEncoder.encode(newPassword));
        userMapper.update(patch);
        log.info("Password reset for user: {}", phone);
    }

    private void recordLoginLog(String userName, String status, String msg) {
        log.info("Recording login log for user: {}, status: {}, msg: {}", userName, status, msg);
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserName(userName);
            loginLog.setStatus(status);
            loginLog.setMsg(msg);
            loginLog.setIpaddr(ServletUtils.getClientIp());
            String ua = ServletUtils.getUserAgent();
            loginLog.setBrowser(ServletUtils.getBrowser(ua));
            loginLog.setOs(ServletUtils.getOs(ua));
            loginLog.setLoginLocation("未知"); // 暂时不实现 IP 转位置
            int rows = loginLogMapper.insert(loginLog);
            log.info("Login log inserted successfully, rows affected: {}", rows);
        } catch (Exception e) {
            log.error("Failed to record login log", e);
        }
    }

    @Override
    public LoginResponse register(RegisterRequest req) {
        String userName = req.getUserName();
        String password = req.getPassword();
        String nickName = req.getNickName();
        String companyName = req.getCompanyName();
        String companyType = req.getCompanyType();
        String phone = StringUtils.hasText(req.getPhonenumber()) ? req.getPhonenumber() : userName;
        
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        // 昵称在前端不再强制：后端兜底用联系人/手机号作为展示名
        if (!StringUtils.hasText(nickName)) {
            nickName = StringUtils.hasText(phone) ? phone.trim() : userName.trim();
        } else {
            nickName = nickName.trim();
        }
        // 图形验证码已在 Controller 层校验，这里不再校验短信验证码

        SysUser existed = userMapper.selectByUserName(userName);
        if (existed != null) {
            throw new ApiException(409, "账号已存在");
        }

        // 判断用户身份
        int isBuyer = req.getIsBuyer() != null ? req.getIsBuyer() : 1;
        int isSeller = req.getIsSeller() != null ? req.getIsSeller() : 0;
        String userType = "SYS_USER";
        if (isBuyer == 1 && isSeller == 1) {
            userType = "CG_GY_USER";
        } else if (isBuyer == 1) {
            userType = "CG_USER";
        } else if (isSeller == 1) {
            userType = "GY_USER";
        }

        SysUser u = new SysUser();
        u.setUserName(userName);
        u.setNickName(nickName);
        u.setPassword(passwordEncoder.encode(password));
        u.setPhonenumber(req.getPhonenumber());
        u.setIsBuyer(isBuyer);
        u.setIsSeller(isSeller);
        u.setUserType(userType);

        int rows = userMapper.insert(u);
        if (rows != 1 || u.getUserId() == null) {
            throw new ApiException(ResultCode.SERVER_ERROR);
        }

        // 可选：注册时自动创建公司并绑定（仅用于演示，提高上手体验）
        if (StringUtils.hasText(companyName)) {
            BusCompany c = new BusCompany();
            c.setOwnerUserId(u.getUserId());
            c.setCompanyName(companyName.trim());
            if (StringUtils.hasText(companyType)) c.setCompanyType(companyType.trim());
            c.setContacts(nickName);
            int cr = companyMapper.insert(c);
            if (cr == 1 && c.getId() != null) {
                SysUser patch = new SysUser();
                patch.setUserId(u.getUserId());
                patch.setCompanyId(c.getId());
                userMapper.update(patch);
            }
        }
        String token = jwtTokenUtil.generateToken(u.getUserId(), u.getUserName());
        return new LoginResponse(token);
    }

    @Override
    public MeResponse me(Long userId) {
        if (userId == null) {
            throw new ApiException(401, "未登录");
        }
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            throw new ApiException(401, "未登录");
        }
        MeResponse r = new MeResponse();
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
        r.setIsAdmin(u.getIsAdmin() != null && u.getIsAdmin() == 1);
        return r;
    }

    @Override
    public boolean checkPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        return userMapper.selectByUserName(phone.trim()) != null;
    }
}


