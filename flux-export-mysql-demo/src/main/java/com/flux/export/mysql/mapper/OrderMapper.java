package com.flux.export.mysql.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flux.export.mysql.pojo.entity.OrderEntity;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
}
