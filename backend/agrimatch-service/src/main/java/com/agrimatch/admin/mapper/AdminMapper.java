package com.agrimatch.admin.mapper;

import com.agrimatch.admin.dto.AdminCompanyResponse;
import com.agrimatch.admin.dto.AdminUserResponse;
import com.agrimatch.points.dto.GiftResponse;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AdminMapper {

    // ==================== 仪表盘 ====================
    long countTotalUsers();
    long countTodayNewUsers();
    long countTotalCompanies();
    long countActiveSupplies();
    long countActiveRequirements();
    long countTotalContracts();
    long countTotalPosts();
    long countTodayLogins();

    // ==================== 用户管理 ====================
    List<AdminUserResponse> selectUsers(@Param("keyword") String keyword,
                                        @Param("offset") int offset,
                                        @Param("size") int size);
    long countUsers(@Param("keyword") String keyword);
    int updateAdminFlag(@Param("userId") Long userId, @Param("isAdmin") Integer isAdmin);
    int updateUserDeleted(@Param("userId") Long userId, @Param("isDeleted") Integer isDeleted);

    // ==================== 企业管理 ====================
    List<AdminCompanyResponse> selectCompanies(@Param("keyword") String keyword,
                                               @Param("status") Integer status,
                                               @Param("offset") int offset,
                                               @Param("size") int size);
    long countCompanies(@Param("keyword") String keyword,
                        @Param("status") Integer status);
    int updateCompanyVerifiedStatus(@Param("id") Long id, @Param("verifiedStatus") Integer verifiedStatus);

    // ==================== 供应审核 ====================
    List<Map<String, Object>> selectSupplies(@Param("keyword") String keyword,
                                             @Param("offset") int offset,
                                             @Param("size") int size);
    long countSupplies(@Param("keyword") String keyword);
    int updateSupplyStatus(@Param("id") Long id, @Param("status") Integer status);

    // ==================== 采购审核 ====================
    List<Map<String, Object>> selectRequirements(@Param("keyword") String keyword,
                                                 @Param("offset") int offset,
                                                 @Param("size") int size);
    long countRequirements(@Param("keyword") String keyword);
    int updateRequirementStatus(@Param("id") Long id, @Param("status") Integer status);

    // ==================== 话题管理 ====================
    List<Map<String, Object>> selectPosts(@Param("keyword") String keyword,
                                          @Param("offset") int offset,
                                          @Param("size") int size);
    long countPosts(@Param("keyword") String keyword);
    int softDeletePost(@Param("id") Long id);

    // ==================== 积分管理 ====================
    BigDecimal sumTotalRechargeAmount();
    BigDecimal sumTodayRechargeAmount();
    BigDecimal sumTotalCardAmount();
    BigDecimal sumTodayCardAmount();
    Long sumTotalGiftPoints();
    Long sumTotalCirculatingPoints();

    List<Map<String, Object>> selectRechargeRecords(@Param("keyword") String keyword,
                                                     @Param("offset") int offset,
                                                     @Param("size") int size);
    long countRechargeRecords(@Param("keyword") String keyword);

    List<Map<String, Object>> selectRechargeUsers(@Param("keyword") String keyword,
                                                   @Param("offset") int offset,
                                                   @Param("size") int size);
    long countRechargeUsers(@Param("keyword") String keyword);

    List<GiftResponse> selectGiftRecords(@Param("keyword") String keyword,
                                          @Param("offset") int offset,
                                          @Param("size") int size);
    long countGiftRecords(@Param("keyword") String keyword);
}
