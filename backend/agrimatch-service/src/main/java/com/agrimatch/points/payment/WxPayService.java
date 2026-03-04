package com.agrimatch.points.payment;

import com.agrimatch.common.exception.ApiException;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.h5.model.*;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.wechat.pay.java.core.exception.ServiceException;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnBean(WxPayConfig.class)
public class WxPayService {

    private static final Logger log = LoggerFactory.getLogger(WxPayService.class);

    private final WxPayProperties props;
    private final NativePayService nativePayService;
    private final H5Service h5Service;
    private final JsapiServiceExtension jsapiServiceExtension;

    public WxPayService(WxPayProperties props,
                        NativePayService nativePayService,
                        H5Service h5Service,
                        JsapiServiceExtension jsapiServiceExtension) {
        this.props = props;
        this.nativePayService = nativePayService;
        this.h5Service = h5Service;
        this.jsapiServiceExtension = jsapiServiceExtension;
    }

    /**
     * Native 下单（PC扫码支付）
     *
     * @param orderNo     我方订单号
     * @param amountCents 金额（分）
     * @param description 商品描述
     * @return code_url 微信支付二维码链接
     */
    public String createNativeOrder(String orderNo, int amountCents, String description) {
        try {
            var request = new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
            request.setAppid(props.getAppId());
            request.setMchid(props.getMchId());
            request.setDescription(description);
            request.setOutTradeNo(orderNo);
            request.setNotifyUrl(props.getNotifyUrl());

            var amount = new com.wechat.pay.java.service.payments.nativepay.model.Amount();
            amount.setTotal(amountCents);
            amount.setCurrency("CNY");
            request.setAmount(amount);

            var response = nativePayService.prepay(request);
            log.info("Native下单成功: orderNo={}", orderNo);
            return response.getCodeUrl();
        } catch (Exception e) {
            String detail = e instanceof ServiceException se ?
                String.format("code=%s, message=%s, body=%s", se.getErrorCode(), se.getErrorMessage(), se.getResponseBody()) : e.getMessage();
            log.error("Native下单失败: orderNo={}, detail={}", orderNo, detail, e);
            throw new ApiException(500, "微信支付下单失败: " + detail);
        }
    }

    /**
     * H5 下单（手机浏览器支付）
     *
     * @param orderNo     我方订单号
     * @param amountCents 金额（分）
     * @param description 商品描述
     * @param clientIp    客户端真实IP
     * @return h5_url 跳转微信支付的URL
     */
    public String createH5Order(String orderNo, int amountCents, String description, String clientIp) {
        try {
            var request = new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
            request.setAppid(props.getAppId());
            request.setMchid(props.getMchId());
            request.setDescription(description);
            request.setOutTradeNo(orderNo);
            request.setNotifyUrl(props.getNotifyUrl());

            var amount = new com.wechat.pay.java.service.payments.h5.model.Amount();
            amount.setTotal(amountCents);
            amount.setCurrency("CNY");
            request.setAmount(amount);

            var sceneInfo = new SceneInfo();
            sceneInfo.setPayerClientIp(clientIp);
            request.setSceneInfo(sceneInfo);

            var response = h5Service.prepay(request);
            log.info("H5下单成功: orderNo={}", orderNo);
            return response.getH5Url();
        } catch (Exception e) {
            String detail = e instanceof ServiceException se ?
                String.format("code=%s, message=%s, body=%s", se.getErrorCode(), se.getErrorMessage(), se.getResponseBody()) : e.getMessage();
            log.error("H5下单失败: orderNo={}, detail={}", orderNo, detail, e);
            throw new ApiException(500, "微信支付下单失败: " + detail);
        }
    }

    /**
     * JSAPI 下单（微信内支付）
     *
     * @param orderNo     我方订单号
     * @param amountCents 金额（分）
     * @param description 商品描述
     * @param openid      用户openid
     * @return 前端拉起支付所需参数
     */
    public Map<String, String> createJsapiOrder(String orderNo, int amountCents, String description, String openid) {
        try {
            var request = new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
            request.setAppid(props.getAppId());
            request.setMchid(props.getMchId());
            request.setDescription(description);
            request.setOutTradeNo(orderNo);
            request.setNotifyUrl(props.getNotifyUrl());

            var amount = new com.wechat.pay.java.service.payments.jsapi.model.Amount();
            amount.setTotal(amountCents);
            amount.setCurrency("CNY");
            request.setAmount(amount);

            var payer = new com.wechat.pay.java.service.payments.jsapi.model.Payer();
            payer.setOpenid(openid);
            request.setPayer(payer);

            PrepayWithRequestPaymentResponse response = jsapiServiceExtension.prepayWithRequestPayment(request);
            log.info("JSAPI下单成功: orderNo={}", orderNo);

            Map<String, String> params = new HashMap<>();
            params.put("appId", response.getAppId());
            params.put("timeStamp", response.getTimeStamp());
            params.put("nonceStr", response.getNonceStr());
            params.put("package", response.getPackageVal());
            params.put("signType", response.getSignType());
            params.put("paySign", response.getPaySign());
            return params;
        } catch (Exception e) {
            String detail = e instanceof ServiceException se ?
                String.format("code=%s, message=%s, body=%s", se.getErrorCode(), se.getErrorMessage(), se.getResponseBody()) : e.getMessage();
            log.error("JSAPI下单失败: orderNo={}, detail={}", orderNo, detail, e);
            throw new ApiException(500, "微信支付下单失败: " + detail);
        }
    }

    /**
     * 查询订单支付状态
     *
     * @param orderNo 我方订单号
     * @return 交易状态
     */
    public Transaction.TradeStateEnum queryOrder(String orderNo) {
        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(props.getMchId());
            request.setOutTradeNo(orderNo);

            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);
            return transaction.getTradeState();
        } catch (Exception e) {
            log.error("查询订单失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
            throw new ApiException(500, "查询订单状态失败，请稍后重试");
        }
    }

    /**
     * 关闭未支付订单
     *
     * @param orderNo 我方订单号
     */
    public void closeOrder(String orderNo) {
        try {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(props.getMchId());
            request.setOutTradeNo(orderNo);

            nativePayService.closeOrder(request);
            log.info("关闭订单成功: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("关闭订单失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
            // 关闭失败不抛异常，仅记录日志（可能已被关闭或已支付）
        }
    }
}
