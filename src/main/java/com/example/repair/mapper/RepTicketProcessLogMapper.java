package com.example.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.repair.entity.RepTicketProcessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单处理记录Mapper接口
 */
@Mapper
public interface RepTicketProcessLogMapper extends BaseMapper<RepTicketProcessLog> {
}
