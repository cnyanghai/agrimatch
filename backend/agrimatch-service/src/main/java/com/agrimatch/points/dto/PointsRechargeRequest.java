package com.agrimatch.points.dto;

import jakarta.validation.constraints.NotNull;

public class PointsRechargeRequest {
    @NotNull
    private Long points;

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
}


