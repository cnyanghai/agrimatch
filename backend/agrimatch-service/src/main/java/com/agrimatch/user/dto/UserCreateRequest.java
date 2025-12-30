package com.agrimatch.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateRequest {
    @NotBlank
    @Size(max = 30)
    private String userName;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    @Size(max = 30)
    private String nickName;

    @Size(max = 11)
    private String phonenumber;

    @Size(max = 50)
    private String wechat;

    private Long companyId;
    private Integer isBuyer;
    private Integer isSeller;
    private String payInfoJson;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public Integer getIsBuyer() {
        return isBuyer;
    }

    public void setIsBuyer(Integer isBuyer) {
        this.isBuyer = isBuyer;
    }

    public Integer getIsSeller() {
        return isSeller;
    }

    public void setIsSeller(Integer isSeller) {
        this.isSeller = isSeller;
    }

    public String getPayInfoJson() {
        return payInfoJson;
    }

    public void setPayInfoJson(String payInfoJson) {
        this.payInfoJson = payInfoJson;
    }
}


