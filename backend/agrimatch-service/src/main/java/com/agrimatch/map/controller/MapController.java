package com.agrimatch.map.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.map.dto.MapCompanyMarkerResponse;
import com.agrimatch.map.mapper.MapCompanyMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {
    private final MapCompanyMapper mapCompanyMapper;

    public MapController(MapCompanyMapper mapCompanyMapper) {
        this.mapCompanyMapper = mapCompanyMapper;
    }

    @GetMapping("/companies")
    public Result<List<MapCompanyMarkerResponse>> companies(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<MapCompanyMapper.Row> rows = mapCompanyMapper.selectCompanyMarkers(kw);
        List<MapCompanyMarkerResponse> out = new ArrayList<>();
        for (MapCompanyMapper.Row r : rows) {
            MapCompanyMarkerResponse o = new MapCompanyMarkerResponse();
            o.setCompanyId(r.getCompanyId());
            o.setCompanyName(r.getCompanyName());
            o.setAddress(r.getAddress());
            o.setLat(r.getLat());
            o.setLng(r.getLng());
            o.setSupplyCount(r.getSupplyCount() == null ? 0 : r.getSupplyCount());
            o.setRequirementCount(r.getRequirementCount() == null ? 0 : r.getRequirementCount());
            o.setSupplyCategories(splitCats(r.getSupplyCats()));
            o.setRequirementCategories(splitCats(r.getRequirementCats()));
            out.add(o);
        }
        return Result.success(out);
    }

    private static List<String> splitCats(String cats) {
        if (!StringUtils.hasText(cats)) return new ArrayList<>();
        return Arrays.stream(cats.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }
}





