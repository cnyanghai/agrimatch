package com.agrimatch.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CompanyCreateRequest {
    @NotBlank
    @Size(max = 128)
    private String companyName;

    @Size(max = 32)
    private String companyType;

    @Size(max = 64)
    private String licenseNo;

    @Size(max = 50)
    private String contacts;

    @Size(max = 20)
    private String phone;

    @Size(max = 50)
    private String wechat;

    @Size(max = 50)
    private String province;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String district;

    @Size(max = 255)
    private String address;

    private BigDecimal lat;
    private BigDecimal lng;

    private String locationsJson;
    private String bankInfoJson;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public String getLocationsJson() {
        return locationsJson;
    }

    public void setLocationsJson(String locationsJson) {
        this.locationsJson = locationsJson;
    }

    public String getBankInfoJson() {
        return bankInfoJson;
    }

    public void setBankInfoJson(String bankInfoJson) {
        this.bankInfoJson = bankInfoJson;
    }
}


