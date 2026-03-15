package com.example.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.repair.common.BusinessException;
import com.example.repair.common.Result;
import com.example.repair.entity.*;
import com.example.repair.mapper.*;
import com.example.repair.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 工单服务实现类
 */
@Service
@RequiredArgsConstructor
public class TicketServiceImpl extends ServiceImpl<RepTicketMapper, RepTicket> implements TicketService {

    private final RepTicketStatusLogMapper statusLogMapper;
    private final RepTicketProcessLogMapper processLogMapper;
    private final RepTicketEvaluationMapper evaluationMapper;
    private final RepTicketCancelMapper cancelMapper;

    private static final DateTimeFormatter TICKET_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepTicket createTicket(RepTicket ticket) {
        // 生成工单编号
        String ticketNo = generateTicketNo();
        ticket.setTicketNo(ticketNo);

        // 设置初始状态
        ticket.setStatus(TicketStatus.PENDING.getCode());
        ticket.setIsEvaluated(0);
        ticket.setIsSlaBreached(0);

        // 保存工单
        this.save(ticket);

        // 记录状态日志
        recordStatusLog(ticket.getId(), null, TicketStatus.PENDING.getCode(),
                null, ticket.getRequesterName(), "CREATE", "创建工单");

        return ticket;
    }

    @Override
    public IPage<RepTicket> getTicketPage(Page<RepTicket> page, String status, String priority, String requesterName) {
        LambdaQueryWrapper<RepTicket> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(RepTicket::getStatus, status);
        }
        if (priority != null && !priority.isEmpty()) {
            wrapper.eq(RepTicket::getPriority, priority);
        }
        if (requesterName != null && !requesterName.isEmpty()) {
            wrapper.like(RepTicket::getRequesterName, requesterName);
        }

        wrapper.orderByDesc(RepTicket::getCreatedAt);

        return this.page(page, wrapper);
    }

    @Override
    public RepTicket getTicketById(Long id) {
        RepTicket ticket = this.getById(id);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }
        return ticket;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTicketStatus(Long id, String newStatus, String operatorName, Long operatorId, String comment) {
        RepTicket ticket = getTicketById(id);
        String oldStatus = ticket.getStatus();

        // 校验状态流转是否合法
        validateStatusTransition(oldStatus, newStatus);

        // 更新工单状态
        ticket.setStatus(newStatus);

        // 如果是开始处理，记录实际开始时间
        if (TicketStatus.IN_PROGRESS.getCode().equals(newStatus)) {
            ticket.setActualStartTime(LocalDateTime.now());
        }

        // 如果是已完成，记录实际完成时间
        if (TicketStatus.RESOLVED.getCode().equals(newStatus)) {
            ticket.setActualCompletionTime(LocalDateTime.now());
        }

        // 如果是已关闭，记录关闭时间
        if (TicketStatus.CLOSED.getCode().equals(newStatus)) {
            ticket.setClosedTime(LocalDateTime.now());
            ticket.setClosedBy(operatorId);
        }

        this.updateById(ticket);

        // 记录状态日志
        recordStatusLog(id, oldStatus, newStatus, operatorId, operatorName, "STATUS_CHANGE", comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTicket(Long id, String assignedTo, Long assignedToId) {
        RepTicket ticket = getTicketById(id);
        String oldStatus = ticket.getStatus();

        // 只有OPEN状态可以分配
        if (!TicketStatus.OPEN.getCode().equals(oldStatus)) {
            throw new BusinessException("只有已创建的工单才能分配");
        }

        // 更新工单
        ticket.setAssignedTo(assignedTo);
        ticket.setAssignedToId(assignedToId);
        ticket.setStatus(TicketStatus.ASSIGNED.getCode());

        this.updateById(ticket);

        // 记录状态日志
        recordStatusLog(id, oldStatus, TicketStatus.ASSIGNED.getCode(),
                assignedToId, assignedTo, "ASSIGN", "分配给" + assignedTo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTicket(Long id, String cancelerName, String cancelReason) {
        RepTicket ticket = getTicketById(id);

        // 只有PENDING、OPEN、ASSIGNED状态可以撤销
        String status = ticket.getStatus();
        if (TicketStatus.PENDING.getCode().equals(status) ||
            TicketStatus.OPEN.getCode().equals(status) ||
            TicketStatus.ASSIGNED.getCode().equals(status)) {

            // 更新工单状态
            ticket.setStatus(TicketStatus.CANCELLED.getCode());
            this.updateById(ticket);

            // 记录状态日志
            recordStatusLog(id, status, TicketStatus.CANCELLED.getCode(),
                    null, cancelerName, "CANCEL", cancelReason);

            // 记录撤销信息
            RepTicketCancel cancel = new RepTicketCancel();
            cancel.setTicketId(id);
            cancel.setCancelerName(cancelerName);
            cancel.setCancelReason(cancelReason);
            cancel.setCancelTime(LocalDateTime.now());
            cancelMapper.insert(cancel);
        } else {
            throw new BusinessException("当前状态不允许撤销");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateTicket(Long id, RepTicketEvaluation evaluation) {
        RepTicket ticket = getTicketById(id);

        // 只有RESOLVED状态可以评价
        if (!TicketStatus.RESOLVED.getCode().equals(ticket.getStatus())) {
            throw new BusinessException("只有已解决的工单才能评价");
        }

        // 检查是否已评价
        if (ticket.getIsEvaluated() == 1) {
            throw new BusinessException("该工单已评价");
        }

        // 保存评价
        evaluation.setTicketId(id);
        evaluationMapper.insert(evaluation);

        // 更新工单评价状态
        ticket.setIsEvaluated(1);
        this.updateById(ticket);
    }

    @Override
    public List<RepTicketStatusLog> getStatusLogs(Long ticketId) {
        return statusLogMapper.selectList(
                new LambdaQueryWrapper<RepTicketStatusLog>()
                        .eq(RepTicketStatusLog::getTicketId, ticketId)
                        .orderByAsc(RepTicketStatusLog::getCreatedAt)
        );
    }

    @Override
    public List<RepTicketProcessLog> getProcessLogs(Long ticketId) {
        return processLogMapper.selectList(
                new LambdaQueryWrapper<RepTicketProcessLog>()
                        .eq(RepTicketProcessLog::getTicketId, ticketId)
                        .orderByAsc(RepTicketProcessLog::getCreatedAt)
        );
    }

    @Override
    public RepTicketEvaluation getEvaluation(Long ticketId) {
        return evaluationMapper.selectOne(
                new LambdaQueryWrapper<RepTicketEvaluation>()
                        .eq(RepTicketEvaluation::getTicketId, ticketId)
        );
    }

    /**
     * 生成工单编号
     */
    private String generateTicketNo() {
        String dateStr = LocalDateTime.now().format(TICKET_NO_FORMATTER);
        // 查询当天最大编号
        LambdaQueryWrapper<RepTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(RepTicket::getTicketNo, "WO-" + dateStr)
                .orderByDesc(RepTicket::getTicketNo)
                .last("LIMIT 1");

        RepTicket lastTicket = this.getOne(wrapper);

        int sequence = 1;
        if (lastTicket != null) {
            String lastNo = lastTicket.getTicketNo();
            String lastSeq = lastNo.substring(lastNo.lastIndexOf("-") + 1);
            sequence = Integer.parseInt(lastSeq) + 1;
        }

        return String.format("WO-%s-%03d", dateStr, sequence);
    }

    /**
     * 记录状态日志
     */
    private void recordStatusLog(Long ticketId, String fromStatus, String toStatus,
                                  Long operatorId, String operatorName, String action, String comment) {
        RepTicketStatusLog log = new RepTicketStatusLog();
        log.setTicketId(ticketId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setAction(action);
        log.setComment(comment);
        statusLogMapper.insert(log);
    }

    /**
     * 校验状态流转是否合法
     */
    private void validateStatusTransition(String fromStatus, String toStatus) {
        // PENDING -> OPEN, CANCELLED, REJECTED
        if (TicketStatus.PENDING.getCode().equals(fromStatus)) {
            if (!TicketStatus.OPEN.getCode().equals(toStatus) &&
                !TicketStatus.CANCELLED.getCode().equals(toStatus)) {
                throw new BusinessException("待审核状态只能转为已创建或撤销");
            }
            return;
        }

        // OPEN -> ASSIGNED, CANCELLED
        if (TicketStatus.OPEN.getCode().equals(fromStatus)) {
            if (!TicketStatus.ASSIGNED.getCode().equals(toStatus) &&
                !TicketStatus.CANCELLED.getCode().equals(toStatus)) {
                throw new BusinessException("已创建状态只能转为已分配或撤销");
            }
            return;
        }

        // ASSIGNED -> IN_PROGRESS, CANCELLED
        if (TicketStatus.ASSIGNED.getCode().equals(fromStatus)) {
            if (!TicketStatus.IN_PROGRESS.getCode().equals(toStatus) &&
                !TicketStatus.CANCELLED.getCode().equals(toStatus)) {
                throw new BusinessException("已分配状态只能转为处理中或撤销");
            }
            return;
        }

        // IN_PROGRESS -> ON_HOLD, RESOLVED
        if (TicketStatus.IN_PROGRESS.getCode().equals(fromStatus)) {
            if (!TicketStatus.ON_HOLD.getCode().equals(toStatus) &&
                !TicketStatus.RESOLVED.getCode().equals(toStatus)) {
                throw new BusinessException("处理中状态只能转为已暂停或已解决");
            }
            return;
        }

        // ON_HOLD -> IN_PROGRESS, RESOLVED
        if (TicketStatus.ON_HOLD.getCode().equals(fromStatus)) {
            if (!TicketStatus.IN_PROGRESS.getCode().equals(toStatus) &&
                !TicketStatus.RESOLVED.getCode().equals(toStatus)) {
                throw new BusinessException("已暂停状态只能转为处理中或已解决");
            }
            return;
        }

        // RESOLVED -> CLOSED, IN_PROGRESS
        if (TicketStatus.RESOLVED.getCode().equals(fromStatus)) {
            if (!TicketStatus.CLOSED.getCode().equals(toStatus) &&
                !TicketStatus.IN_PROGRESS.getCode().equals(toStatus)) {
                throw new BusinessException("已解决状态只能转为已关闭或重新打开");
            }
        }
    }
}
