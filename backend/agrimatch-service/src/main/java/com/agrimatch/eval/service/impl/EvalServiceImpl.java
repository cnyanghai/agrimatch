package com.agrimatch.eval.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.deal.domain.BusDeal;
import com.agrimatch.deal.mapper.DealMapper;
import com.agrimatch.eval.domain.BusOrderEval;
import com.agrimatch.eval.dto.EvalCreateRequest;
import com.agrimatch.eval.dto.EvalResponse;
import com.agrimatch.eval.mapper.EvalMapper;
import com.agrimatch.eval.service.EvalService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EvalServiceImpl implements EvalService {
    private final EvalMapper evalMapper;
    private final DealMapper dealMapper;

    public EvalServiceImpl(EvalMapper evalMapper, DealMapper dealMapper) {
        this.evalMapper = evalMapper;
        this.dealMapper = dealMapper;
    }

    @Override
    public Long create(Long fromUserId, EvalCreateRequest req) {
        if (fromUserId == null) throw new ApiException(401, "未登录");
        if (req == null || req.getDealId() == null || req.getToCompanyId() == null || req.getStars() == null) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        BusDeal d = dealMapper.selectById(req.getDealId());
        if (d == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "成交不存在");
        if (d.getStatus() == null || d.getStatus() != 1) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "该成交状态不允许评价");
        }
        // 仅成交双方可评价；且只能评价对方公司
        Long expectedToCompanyId;
        if (fromUserId.equals(d.getBuyerUserId())) {
            expectedToCompanyId = d.getSellerCompanyId();
        } else if (fromUserId.equals(d.getSellerUserId())) {
            expectedToCompanyId = d.getBuyerCompanyId();
        } else {
            throw new ApiException(403, "仅成交双方可评价");
        }
        if (!expectedToCompanyId.equals(req.getToCompanyId())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "被评价公司不匹配该成交对手方");
        }
        int existed = evalMapper.countByDealAndFromUser(req.getDealId(), fromUserId);
        if (existed > 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "你已对该成交评价过");
        }

        BusOrderEval e = new BusOrderEval();
        e.setFromUserId(fromUserId);
        e.setToCompanyId(req.getToCompanyId());
        e.setDealId(req.getDealId());
        e.setRequirementId(d.getRequirementId());
        e.setSupplyId(d.getSupplyId());
        e.setStars(req.getStars());
        e.setComment(StringUtils.hasText(req.getComment()) ? req.getComment() : null);
        e.setImagesJson(StringUtils.hasText(req.getImagesJson()) ? req.getImagesJson() : null);

        int rows = evalMapper.insert(e);
        if (rows != 1 || e.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return e.getId();
    }

    @Override
    public List<EvalResponse> listByCompany(Long toCompanyId) {
        if (toCompanyId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        List<BusOrderEval> list = evalMapper.selectByCompany(toCompanyId);
        List<EvalResponse> out = new ArrayList<>();
        for (BusOrderEval e : list) out.add(toResponse(e));
        return out;
    }

    private static EvalResponse toResponse(BusOrderEval e) {
        EvalResponse r = new EvalResponse();
        r.setId(e.getId());
        r.setRequirementId(e.getRequirementId());
        r.setSupplyId(e.getSupplyId());
        r.setFromUserId(e.getFromUserId());
        r.setToCompanyId(e.getToCompanyId());
        r.setFromUserName(e.getFromUserName());
        r.setFromNickName(e.getFromNickName());
        r.setToCompanyName(e.getToCompanyName());
        r.setStars(e.getStars());
        r.setComment(e.getComment());
        r.setImagesJson(e.getImagesJson());
        r.setCreateTime(e.getCreateTime());
        return r;
    }
}


