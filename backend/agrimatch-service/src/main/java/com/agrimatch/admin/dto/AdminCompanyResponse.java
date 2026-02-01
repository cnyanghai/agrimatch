package com.agrimatch.admin.dto;

public class AdminCompanyResponse {
    private Long id;
    private String companyName;
    private String companyType;
    private Long ownerUserId;
    private String ownerName;
    private String contacts;
    private String phone;
    private String province;
    private String city;
    private String district;
    private Integer verifiedStatus;
    private String createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getContacts() { return contacts; }
    public void setContacts(String contacts) { this.contacts = contacts; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public Integer getVerifiedStatus() { return verifiedStatus; }
    public void setVerifiedStatus(Integer verifiedStatus) { this.verifiedStatus = verifiedStatus; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
