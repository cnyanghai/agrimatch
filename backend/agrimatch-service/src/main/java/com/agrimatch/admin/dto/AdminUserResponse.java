package com.agrimatch.admin.dto;

public class AdminUserResponse {
    private Long userId;
    private String userName;
    private String nickName;
    private String phonenumber;
    private Long companyId;
    private String companyName;
    private Integer isBuyer;
    private Integer isSeller;
    private String userType;
    private Integer isAdmin;
    private Integer isDeleted;
    private String createTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getIsBuyer() { return isBuyer; }
    public void setIsBuyer(Integer isBuyer) { this.isBuyer = isBuyer; }

    public Integer getIsSeller() { return isSeller; }
    public void setIsSeller(Integer isSeller) { this.isSeller = isSeller; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Integer getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Integer isAdmin) { this.isAdmin = isAdmin; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
