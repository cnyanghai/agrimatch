package com.agrimatch.points.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.points.domain.BusJdRedeem;
import com.agrimatch.points.domain.BusPointsAccount;
import com.agrimatch.points.domain.BusPointsTx;
import com.agrimatch.points.domain.BusRechargeOrder;
import com.agrimatch.points.dto.*;
import com.agrimatch.points.mapper.PointsMapper;
import com.agrimatch.points.service.PointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PointsServiceImpl implements PointsService {
    private static final Logger log = LoggerFactory.getLogger(PointsServiceImpl.class);
    private static final BigDecimal RATE = BigDecimal.ONE; // 1积分=1元（演示）

    // 限额配置
    private static final int RECHARGE_MAX = 10000;
    private static final int RECHARGE_DAY_MAX = 50000;
    private static final int REDEEM_MAX = 5000;
    private static final int REDEEM_DAY_MAX = 10000;
    private static final Set<Integer> JD_FACE_VALUES = Set.of(50, 100, 200, 500);

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

    @Override
    public Long getBalance(Long userId) {
        if (userId == null) return 0L;
        ensureAccount(userId);
        BusPointsAccount a = pointsMapper.selectAccountByUserId(userId);
        return a == null || a.getPointsBalance() == null ? 0L : a.getPointsBalance();
    }

    @Override
    @Transactional
    public void deduct(Long userId, Long points, String remark) {
        if (points == null || points <= 0) throw new ApiException(ResultCode.PARAM_ERROR);
        ensureAccount(userId);

        BusPointsAccount a = pointsMapper.selectAccountByUserIdForUpdate(userId);
        long cur = a.getPointsBalance() == null ? 0 : a.getPointsBalance();
        if (cur < points) throw new ApiException(400, "积分不足");

        long newPoints = cur - points;
        int rows = pointsMapper.updateAccountBalance(userId, newPoints, a.getCnyBalance());
        if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);

        BusPointsTx tx = new BusPointsTx();
        tx.setUserId(userId);
        tx.setTxType("BOUNTY_DEDUCT");
        tx.setPointsDelta(-points);
        tx.setCnyDelta(BigDecimal.ZERO);
        tx.setRemark(remark);
        pointsMapper.insertTx(tx);
    }

    @Override
    @Transactional
    public void add(Long userId, Long points, String remark) {
        if (points == null || points <= 0) throw new ApiException(ResultCode.PARAM_ERROR);
        ensureAccount(userId);

        BusPointsAccount a = pointsMapper.selectAccountByUserIdForUpdate(userId);
        long newPoints = safeAdd(a.getPointsBalance(), points);
        int rows = pointsMapper.updateAccountBalance(userId, newPoints, a.getCnyBalance());
        if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);

        BusPointsTx tx = new BusPointsTx();
        tx.setUserId(userId);
        tx.setTxType("BOUNTY_REWARD");
        tx.setPointsDelta(points);
        tx.setCnyDelta(BigDecimal.ZERO);
        tx.setRemark(remark);
        pointsMapper.insertTx(tx);
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

    // ================= 充值订单 =================

    @Override
    @Transactional
    public RechargeCreateResponse createRechargeOrder(Long userId, RechargeCreateRequest req) {
        if (req.getAmount() == null || req.getAmount() <= 0 || req.getAmount() > RECHARGE_MAX) {
            throw new ApiException(400, "充值金额不合法");
        }

        // 检查日限额
        Integer todayTotal = pointsMapper.sumTodayRechargeByUserId(userId);
        if (todayTotal == null) todayTotal = 0;
        if (todayTotal + req.getAmount() > RECHARGE_DAY_MAX) {
            throw new ApiException(400, "今日充值已达上限 " + RECHARGE_DAY_MAX + " 元");
        }

        // 生成订单号
        String orderNo = "R" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BusRechargeOrder order = new BusRechargeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAmount(BigDecimal.valueOf(req.getAmount()));
        order.setPoints(req.getAmount()); // 1元=1积分
        order.setPayChannel(req.getPayChannel());
        order.setStatus(0); // 待支付

        pointsMapper.insertRechargeOrder(order);
        log.info("创建充值订单: orderNo={}, userId={}, amount={}", orderNo, userId, req.getAmount());

        // TODO: 调用微信/支付宝接口获取真实二维码
        // 这里返回模拟二维码
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + orderNo;

        RechargeCreateResponse resp = new RechargeCreateResponse();
        resp.setOrderNo(orderNo);
        resp.setQrCodeUrl(qrCodeUrl);
        resp.setAmount(req.getAmount());
        resp.setPoints(req.getAmount());
        return resp;
    }

    @Override
    public Integer getRechargeOrderStatus(String orderNo) {
        BusRechargeOrder order = pointsMapper.selectRechargeOrderByOrderNo(orderNo);
        return order == null ? null : order.getStatus();
    }

    @Override
    @Transactional
    public void confirmRechargeOrder(String orderNo, String tradeNo) {
        BusRechargeOrder order = pointsMapper.selectRechargeOrderByOrderNo(orderNo);
        if (order == null) throw new ApiException(404, "订单不存在");
        if (order.getStatus() != 0) {
            log.warn("订单状态不是待支付，忽略回调: orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        // 更新订单状态
        pointsMapper.updateRechargeOrderStatus(orderNo, 1, tradeNo);

        // 充值积分
        recharge(order.getUserId(), order.getPoints().longValue());

        log.info("充值订单支付成功: orderNo={}, userId={}, points={}", orderNo, order.getUserId(), order.getPoints());
    }

    // ================= 京东卡兑换 =================

    @Override
    @Transactional
    public JdRedeemResponse redeemJdCard(Long userId, JdRedeemRequest req) {
        if (!JD_FACE_VALUES.contains(req.getFaceValue())) {
            throw new ApiException(400, "不支持的面额");
        }

        // TODO: 验证短信验证码（当前模拟）
        // smsCodeService.verifyOrThrow(phone, 3, req.getSmsCode());

        // 检查日限额
        Integer todayTotal = pointsMapper.sumTodayJdRedeemByUserId(userId);
        if (todayTotal == null) todayTotal = 0;
        if (todayTotal + req.getFaceValue() > REDEEM_DAY_MAX) {
            throw new ApiException(400, "今日兑换已达上限 " + REDEEM_DAY_MAX + " 积分");
        }

        // 检查余额
        ensureAccount(userId);
        BusPointsAccount account = pointsMapper.selectAccountByUserIdForUpdate(userId);
        long curPoints = account.getPointsBalance() == null ? 0 : account.getPointsBalance();
        if (curPoints < req.getFaceValue()) {
            throw new ApiException(400, "积分余额不足");
        }

        // 扣减积分
        long newPoints = curPoints - req.getFaceValue();
        pointsMapper.updateAccountBalance(userId, newPoints, account.getCnyBalance());

        // 记录流水
        BusPointsTx tx = new BusPointsTx();
        tx.setUserId(userId);
        tx.setTxType("JD_REDEEM");
        tx.setPointsDelta((long) -req.getFaceValue());
        tx.setCnyDelta(BigDecimal.ZERO);
        tx.setRemark("兑换京东卡 ¥" + req.getFaceValue());
        pointsMapper.insertTx(tx);

        // TODO: 调用京东卡API获取真实卡密
        // 这里返回模拟卡密
        String cardCode = "JD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 记录兑换
        BusJdRedeem redeem = new BusJdRedeem();
        redeem.setUserId(userId);
        redeem.setPointsCost(req.getFaceValue());
        redeem.setFaceValue(req.getFaceValue());
        redeem.setCardCode(cardCode); // TODO: 生产环境需加密存储
        redeem.setStatus(1); // 成功
        pointsMapper.insertJdRedeem(redeem);

        log.info("京东卡兑换成功: userId={}, faceValue={}, cardCode={}", userId, req.getFaceValue(), cardCode);

        JdRedeemResponse resp = new JdRedeemResponse();
        resp.setRedeemId(redeem.getId());
        resp.setCardCode(cardCode);
        resp.setFaceValue(req.getFaceValue());
        resp.setPointsCost(req.getFaceValue());
        resp.setNewPointsBalance(newPoints);
        return resp;
    }

    @Override
    public PointsLimitsResponse getLimits(Long userId) {
        PointsLimitsResponse resp = new PointsLimitsResponse();
        resp.setRechargeMax(RECHARGE_MAX);
        resp.setRechargeDayMax(RECHARGE_DAY_MAX);
        resp.setRedeemMax(REDEEM_MAX);
        resp.setRedeemDayMax(REDEEM_DAY_MAX);

        if (userId != null) {
            Integer todayRecharge = pointsMapper.sumTodayRechargeByUserId(userId);
            Integer todayRedeem = pointsMapper.sumTodayJdRedeemByUserId(userId);
            resp.setTodayRechargeTotal(todayRecharge == null ? 0 : todayRecharge);
            resp.setTodayRedeemTotal(todayRedeem == null ? 0 : todayRedeem);
        }

        return resp;
    }
}


