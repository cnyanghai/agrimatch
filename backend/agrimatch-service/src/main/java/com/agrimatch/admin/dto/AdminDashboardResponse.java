package com.agrimatch.admin.dto;

public class AdminDashboardResponse {
    private long totalUsers;
    private long todayNewUsers;
    private long totalCompanies;
    private long activeSupplyCount;
    private long activeRequirementCount;
    private long totalContracts;
    private long totalPosts;
    private long todayLoginCount;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTodayNewUsers() { return todayNewUsers; }
    public void setTodayNewUsers(long todayNewUsers) { this.todayNewUsers = todayNewUsers; }

    public long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(long totalCompanies) { this.totalCompanies = totalCompanies; }

    public long getActiveSupplyCount() { return activeSupplyCount; }
    public void setActiveSupplyCount(long activeSupplyCount) { this.activeSupplyCount = activeSupplyCount; }

    public long getActiveRequirementCount() { return activeRequirementCount; }
    public void setActiveRequirementCount(long activeRequirementCount) { this.activeRequirementCount = activeRequirementCount; }

    public long getTotalContracts() { return totalContracts; }
    public void setTotalContracts(long totalContracts) { this.totalContracts = totalContracts; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public long getTodayLoginCount() { return todayLoginCount; }
    public void setTodayLoginCount(long todayLoginCount) { this.todayLoginCount = todayLoginCount; }
}
