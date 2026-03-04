package com.agrimatch.points.payment;

import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.agrimatch.points.domain.BusRechargeOrder;
import com.agrimatch.points.mapper.PointsMapper;
import com.agrimatch.points.service.PointsService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pay/wechat")
@ConditionalOnBean(WxPayConfig.class)
public class WxPayCallbackController {

    private static final Logger log = LoggerFactory.getLogger(WxPayCallbackController.class);

    private final NotificationParser notificationParser;
    private final PointsMapper pointsMapper;
    private final PointsService pointsService;

    public WxPayCallbackController(NotificationParser notificationParser,
                                   PointsMapper pointsMapper,
                                   PointsService pointsService) {
        this.notificationParser = notificationParser;
        this.pointsMapper = pointsMapper;
        this.pointsService = pointsService;
    }

    @PostMapping("/notify")
    @Transactional
    public ResponseEntity<Map<String, String>> onNotify(
            HttpServletRequest request,
            @RequestBody String body) {
        try {
            // 1. 解析并验签
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .body(body)
                    .build();

            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            String orderNo = transaction.getOutTradeNo();
            String tradeNo = transaction.getTransactionId();
            Transaction.TradeStateEnum tradeState = transaction.getTradeState();

            log.info("收到微信支付回调: orderNo={}, tradeState={}, tradeNo={}", orderNo, tradeState, tradeNo);

            // 2. 仅处理支付成功
            if (tradeState == Transaction.TradeStateEnum.SUCCESS) {
                // 查询订单
                BusRechargeOrder order = pointsMapper.selectRechargeOrderByOrderNo(orderNo);
                if (order == null) {
                    log.warn("回调订单不存在: orderNo={}", orderNo);
                    return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "OK"));
                }

                // 幂等检查：已支付则直接返回成功
                if (order.getStatus() == 1) {
                    log.info("订单已支付，忽略重复回调: orderNo={}", orderNo);
                    return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "OK"));
                }

                // 乐观锁更新：UPDATE WHERE status = 0
                int rows = pointsMapper.updateRechargeOrderStatusWithLock(orderNo, 1, tradeNo);
                if (rows == 1) {
                    // 更新成功，充值积分
                    pointsService.recharge(order.getUserId(), order.getPoints().longValue());
                    log.info("充值订单回调处理成功: orderNo={}, userId={}, points={}",
                            orderNo, order.getUserId(), order.getPoints());
                } else {
                    log.info("订单状态已变更，跳过充值: orderNo={}", orderNo);
                }
            }

            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "OK"));

        } catch (Exception e) {
            log.error("微信支付回调处理失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "FAIL", "message", "处理失败"));
        }
    }
}
