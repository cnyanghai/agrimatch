package com.agrimatch.company.dto;

import com.agrimatch.supply.dto.SupplyResponse;
import com.agrimatch.requirement.dto.RequirementResponse;
import java.util.List;

public class CompanyProfileResponse {
    private CompanyResponse company;
    private List<SupplyResponse> supplies;
    private List<RequirementResponse> requirements;

    public CompanyResponse getCompany() {
        return company;
    }

    public void setCompany(CompanyResponse company) {
        this.company = company;
    }

    public List<SupplyResponse> getSupplies() {
        return supplies;
    }

    public void setSupplies(List<SupplyResponse> supplies) {
        this.supplies = supplies;
    }

    public List<RequirementResponse> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<RequirementResponse> requirements) {
        this.requirements = requirements;
    }
}

