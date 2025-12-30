package com.agrimatch.user.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @Size(max = 30)
    private String nickName;

    @Size(max = 11)
    private String phonenumber;

    @Size(max = 50)
    private String wechat;

    private Long companyId;
    private String payInfoJson;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getPayInfoJson() {
        return payInfoJson;
    }

    public void setPayInfoJson(String payInfoJson) {
        this.payInfoJson = payInfoJson;
    }
}


