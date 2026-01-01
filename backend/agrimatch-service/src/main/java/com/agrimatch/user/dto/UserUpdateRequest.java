package com.agrimatch.user.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @Size(max = 30)
    private String nickName;

    @Size(max = 11)
    private String phonenumber;

    @Size(max = 50)
    private String wechat;

    @Size(max = 50)
    private String position;

    private LocalDate birthDate;

    private Integer gender;

    @Size(max = 500)
    private String bio;

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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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


