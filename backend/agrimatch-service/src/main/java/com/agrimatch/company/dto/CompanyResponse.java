package com.agrimatch.company.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompanyResponse {
    private Long id;
    private Long ownerUserId;
    private String companyName;
    private String companyType;
    private String licenseNo;
    private String licenseImgUrl;
    private String legalPerson;
    private String businessScope;
    private String contacts;
    private String phone;
    private String wechat;
    private String province;
    private String city;
    private String district;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private String locationsJson;
    private String bankInfoJson;
    private String registeredCapital;
    private String establishDate;
    private String scale;
    private String companyIntro;
    private String tagsJson;
    private String announcementsJson;
    private String recruitmentJson;
    private String certificatesJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

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

    public String getLicenseImgUrl() {
        return licenseImgUrl;
    }

    public void setLicenseImgUrl(String licenseImgUrl) {
        this.licenseImgUrl = licenseImgUrl;
    }

    public String getLegalPerson() {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson) {
        this.legalPerson = legalPerson;
    }

    public String getBusinessScope() {
        return businessScope;
    }

    public void setBusinessScope(String businessScope) {
        this.businessScope = businessScope;
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

    public String getRegisteredCapital() {
        return registeredCapital;
    }

    public void setRegisteredCapital(String registeredCapital) {
        this.registeredCapital = registeredCapital;
    }

    public String getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(String establishDate) {
        this.establishDate = establishDate;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getCompanyIntro() {
        return companyIntro;
    }

    public void setCompanyIntro(String companyIntro) {
        this.companyIntro = companyIntro;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public String getAnnouncementsJson() {
        return announcementsJson;
    }

    public void setAnnouncementsJson(String announcementsJson) {
        this.announcementsJson = announcementsJson;
    }

    public String getRecruitmentJson() {
        return recruitmentJson;
    }

    public void setRecruitmentJson(String recruitmentJson) {
        this.recruitmentJson = recruitmentJson;
    }

    public String getCertificatesJson() {
        return certificatesJson;
    }

    public void setCertificatesJson(String certificatesJson) {
        this.certificatesJson = certificatesJson;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}


