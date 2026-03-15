package com.example.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.repair.entity.RepTicketStatusLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单状态日志Mapper接口
 */
@Mapper
public interface RepTicketStatusLogMapper extends BaseMapper<RepTicketStatusLog> {
}
