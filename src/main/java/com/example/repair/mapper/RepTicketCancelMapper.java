package com.example.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.repair.entity.RepTicketCancel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单撤销记录Mapper接口
 */
@Mapper
public interface RepTicketCancelMapper extends BaseMapper<RepTicketCancel> {
}
