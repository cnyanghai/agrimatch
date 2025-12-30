package com.agrimatch.home.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.home.dto.StatsResponse;
import com.agrimatch.home.mapper.StatsMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class StatsController {
    private final StatsMapper statsMapper;

    public StatsController(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    @GetMapping("/stats")
    public Result<StatsResponse> stats() {
        StatsResponse s = new StatsResponse();
        s.setUserCount(statsMapper.userCount());
        s.setRequirementCount(statsMapper.requirementCount());
        s.setSupplyCount(statsMapper.supplyCount());
        return Result.success(s);
    }
}


