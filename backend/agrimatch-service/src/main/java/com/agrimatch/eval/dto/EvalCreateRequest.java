package com.agrimatch.eval.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EvalCreateRequest {
    @NotNull
    private Long dealId;
    private Long requirementId;
    private Long supplyId;

    @NotNull
    private Long toCompanyId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer stars;

    @Size(max = 500)
    private String comment;

    private String imagesJson;

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public Long getToCompanyId() {
        return toCompanyId;
    }

    public void setToCompanyId(Long toCompanyId) {
        this.toCompanyId = toCompanyId;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
    }
}


