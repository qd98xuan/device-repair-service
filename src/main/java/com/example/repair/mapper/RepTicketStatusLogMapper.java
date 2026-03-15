package com.example.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.repair.entity.RepTicketStatusLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * 工单状态日志Mapper接口
 */
@Mapper
public interface RepTicketStatusLogMapper extends BaseMapper<RepTicketStatusLog> {

    @Insert("""
            INSERT INTO rep_ticket_status_logs (
                ticket_id, from_status, to_status, operator_id, operator_name, action, comment
            ) VALUES (
                #{log.ticketId},
                #{log.fromStatus},
                #{log.toStatus},
                #{log.operatorId},
                #{log.operatorName},
                #{log.action},
                #{log.comment}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "log.id")
    int insertStatusLog(@Param("log") RepTicketStatusLog log);
}
