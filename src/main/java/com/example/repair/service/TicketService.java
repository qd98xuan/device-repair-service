package com.example.repair.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.repair.entity.RepTicket;
import com.example.repair.entity.RepTicketEvaluation;
import com.example.repair.entity.RepTicketProcessLog;
import com.example.repair.entity.RepTicketStatusLog;

/**
 * 工单服务接口
 */
public interface TicketService extends IService<RepTicket> {

    /**
     * 创建工单
     */
    RepTicket createTicket(RepTicket ticket);

    /**
     * 分页查询工单
     */
    IPage<RepTicket> getTicketPage(Page<RepTicket> page, String status, String priority, String requesterName);

    /**
     * 获取工单详情
     */
    RepTicket getTicketById(Long id);

    /**
     * 更新工单状态
     */
    void updateTicketStatus(Long id, String status, String operatorName, Long operatorId, String comment);

    /**
     * 分配工单
     */
    void assignTicket(Long id, String assignedTo, Long assignedToId);

    /**
     * 撤销工单
     */
    void cancelTicket(Long id, String cancelerName, String cancelReason);

    /**
     * 评价工单
     */
    void evaluateTicket(Long id, RepTicketEvaluation evaluation);

    /**
     * 获取工单状态日志
     */
    java.util.List<RepTicketStatusLog> getStatusLogs(Long ticketId);

    /**
     * 获取工单处理记录
     */
    java.util.List<RepTicketProcessLog> getProcessLogs(Long ticketId);

    /**
     * 获取工单评价
     */
    RepTicketEvaluation getEvaluation(Long ticketId);
}
