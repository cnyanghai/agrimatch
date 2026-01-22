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
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final CompanyMapper companyMapper;
    private final SmsCodeService smsCodeService;

    public AuthServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenUtil jwtTokenUtil,
                           CompanyMapper companyMapper,
                           SmsCodeService smsCodeService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.companyMapper = companyMapper;
        this.smsCodeService = smsCodeService;
    }

    @Override
    public LoginResponse login(String userName, String password) {
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        SysUser u = userMapper.selectByUserName(userName);
        if (u == null || !StringUtils.hasText(u.getPassword())) {
            throw new ApiException(401, "账号或密码错误");
        }
        if (!passwordEncoder.matches(password, u.getPassword())) {
            throw new ApiException(401, "账号或密码错误");
        }
        String token = jwtTokenUtil.generateToken(u.getUserId(), u.getUserName());
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
            throw new ApiException(404, "账号不存在，请先注册");
        }
        String token = jwtTokenUtil.generateToken(u.getUserId(), u.getUserName());
        return new LoginResponse(token);
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
        return r;
    }
}


