package com.example.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.repair.entity.RepTicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单Mapper接口
 */
@Mapper
public interface RepTicketMapper extends BaseMapper<RepTicket> {
}
