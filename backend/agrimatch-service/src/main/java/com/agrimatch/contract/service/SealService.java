package com.agrimatch.contract.service;

import com.agrimatch.contract.domain.BusCompanySeal;

import java.util.List;

/**
 * 公司电子章管理服务
 */
public interface SealService {
    
    /**
     * 上传公章图片
     */
    Long uploadSeal(Long userId, String sealName, String sealType, String sealUrl);
    
    /**
     * 生成电子章（根据公司名称）
     */
    Long generateSeal(Long userId, String sealName, String sealType);
    
    /**
     * 获取公司的所有电子章
     */
    List<BusCompanySeal> listByCompany(Long userId);
    
    /**
     * 获取公司的默认电子章
     */
    BusCompanySeal getDefault(Long userId);
    
    /**
     * 设置默认电子章
     */
    void setDefault(Long userId, Long sealId);
    
    /**
     * 删除电子章
     */
    void delete(Long userId, Long sealId);
}

