package com.agrimatch.points.dto;

import jakarta.validation.constraints.NotNull;

public class PointsRedeemRequest {
    @NotNull
    private Long points;

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
}


