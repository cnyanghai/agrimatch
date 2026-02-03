package com.agrimatch.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    private String smsCode;

    @NotBlank
    @Size(min = 6, max = 64)
    private String newPassword;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSmsCode() { return smsCode; }
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
