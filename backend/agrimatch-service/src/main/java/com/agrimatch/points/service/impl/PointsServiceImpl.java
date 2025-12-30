package com.agrimatch.points.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.points.domain.BusPointsAccount;
import com.agrimatch.points.domain.BusPointsTx;
import com.agrimatch.points.dto.PointsMeResponse;
import com.agrimatch.points.dto.PointsTxResponse;
import com.agrimatch.points.mapper.PointsMapper;
import com.agrimatch.points.service.PointsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PointsServiceImpl implements PointsService {
    private static final BigDecimal RATE = BigDecimal.ONE; // 1积分=1元（演示）

    private final PointsMapper pointsMapper;

    public PointsServiceImpl(PointsMapper pointsMapper) {
        this.pointsMapper = pointsMapper;
    }

    @Override
    @Transactional
    public PointsMeResponse me(Long userId) {
        ensureAccount(userId);
        BusPointsAccount a = pointsMapper.selectAccountByUserId(userId);
        return toMe(a);
    }

    @Override
    @Transactional
    public PointsMeResponse recharge(Long userId, Long points) {
        if (points == null || points <= 0) throw new ApiException(ResultCode.PARAM_ERROR);
        ensureAccount(userId);

        BusPointsAccount a = pointsMapper.selectAccountByUserIdForUpdate(userId);
        long newPoints = safeAdd(a.getPointsBalance(), points);
        BigDecimal newCny = a.getCnyBalance();

        int rows = pointsMapper.updateAccountBalance(userId, newPoints, newCny);
        if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);

        BusPointsTx tx = new BusPointsTx();
        tx.setUserId(userId);
        tx.setTxType("RECHARGE");
        tx.setPointsDelta(points);
        tx.setCnyDelta(BigDecimal.ZERO);
        tx.setRemark("recharge");
        pointsMapper.insertTx(tx);

        BusPointsAccount out = pointsMapper.selectAccountByUserId(userId);
        return toMe(out);
    }

    @Override
    @Transactional
    public PointsMeResponse redeem(Long userId, Long points) {
        if (points == null || points <= 0) throw new ApiException(ResultCode.PARAM_ERROR);
        ensureAccount(userId);

        BusPointsAccount a = pointsMapper.selectAccountByUserIdForUpdate(userId);
        long cur = a.getPointsBalance() == null ? 0 : a.getPointsBalance();
        if (cur < points) throw new ApiException(400, "积分不足");

        long newPoints = cur - points;
        BigDecimal cnyAdd = RATE.multiply(BigDecimal.valueOf(points));
        BigDecimal newCny = a.getCnyBalance().add(cnyAdd);

        int rows = pointsMapper.updateAccountBalance(userId, newPoints, newCny);
        if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);

        BusPointsTx tx = new BusPointsTx();
        tx.setUserId(userId);
        tx.setTxType("REDEEM");
        tx.setPointsDelta(-points);
        tx.setCnyDelta(cnyAdd);
        tx.setRemark("redeem");
        pointsMapper.insertTx(tx);

        BusPointsAccount out = pointsMapper.selectAccountByUserId(userId);
        return toMe(out);
    }

    @Override
    public List<PointsTxResponse> myTx(Long userId) {
        List<BusPointsTx> list = pointsMapper.selectTxListByUserId(userId);
        List<PointsTxResponse> out = new ArrayList<>();
        for (BusPointsTx tx : list) {
            PointsTxResponse r = new PointsTxResponse();
            r.setId(tx.getId());
            r.setTxType(tx.getTxType());
            r.setPointsDelta(tx.getPointsDelta());
            r.setCnyDelta(tx.getCnyDelta());
            r.setRemark(tx.getRemark());
            r.setCreateTime(tx.getCreateTime());
            out.add(r);
        }
        return out;
    }

    private void ensureAccount(Long userId) {
        if (userId == null) throw new ApiException(401, "未登录");
        BusPointsAccount existed = pointsMapper.selectAccountByUserId(userId);
        if (existed != null) return;
        BusPointsAccount a = new BusPointsAccount();
        a.setUserId(userId);
        a.setPointsBalance(0L);
        a.setCnyBalance(BigDecimal.ZERO);
        try {
            pointsMapper.insertAccount(a);
        } catch (Exception ignore) {
            // 并发下可能重复插入，忽略后续 select 即可
        }
    }

    private static PointsMeResponse toMe(BusPointsAccount a) {
        if (a == null) throw new ApiException(ResultCode.SERVER_ERROR);
        PointsMeResponse r = new PointsMeResponse();
        r.setPointsBalance(a.getPointsBalance() == null ? 0L : a.getPointsBalance());
        r.setCnyBalance(a.getCnyBalance() == null ? BigDecimal.ZERO : a.getCnyBalance());
        return r;
    }

    private static long safeAdd(Long a, Long b) {
        long x = a == null ? 0L : a;
        long y = b == null ? 0L : b;
        long r = x + y;
        if (((x ^ r) & (y ^ r)) < 0) throw new ApiException(400, "积分数值溢出");
        return r;
    }
}


