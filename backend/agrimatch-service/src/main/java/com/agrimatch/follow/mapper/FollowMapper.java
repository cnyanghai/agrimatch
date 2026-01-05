package com.agrimatch.follow.mapper;

import com.agrimatch.follow.domain.BusUserFollow;
import com.agrimatch.follow.dto.FollowedUserResponse;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.supply.dto.SupplyResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {

    /**
     * 插入关注关系
     */
    int insert(BusUserFollow follow);

    /**
     * 删除关注关系
     */
    int deleteByUserAndFollowUser(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    /**
     * 检查是否已关注
     */
    BusUserFollow selectByUserAndFollowUser(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    /**
     * 获取用户关注的人列表（含用户信息和公司信息）
     */
    List<FollowedUserResponse> selectFollowedUsers(@Param("userId") Long userId);

    /**
     * 获取关注用户发布的采购需求
     */
    List<RequirementResponse> selectFollowedRequirements(@Param("userId") Long userId);

    /**
     * 获取关注用户发布的供应信息
     */
    List<SupplyResponse> selectFollowedSupplies(@Param("userId") Long userId);

    /**
     * 获取关注某用户的粉丝数
     */
    int countFollowers(@Param("followUserId") Long followUserId);

    /**
     * 获取用户关注的人数
     */
    int countFollowing(@Param("userId") Long userId);
}

