package com.agrimatch.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    @Size(max = 30)
    private String userName;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    @Size(max = 30)
    private String nickName;

    @Size(max = 128)
    private String companyName;

    @Size(max = 11)
    private String phonenumber;

    private String smsCode;

    private Integer isBuyer;

    private Integer isSeller;

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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
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
}


