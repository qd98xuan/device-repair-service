package com.example.repair.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.repair.common.Result;
import com.example.repair.entity.RepTicket;
import com.example.repair.entity.RepTicketEvaluation;
import com.example.repair.entity.RepTicketProcessLog;
import com.example.repair.entity.RepTicketStatusLog;
import com.example.repair.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单控制器
 */
@Tag(name = "工单管理", description = "工单的创建、查询、状态更新等接口")
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "创建工单", description = "创建一个新的报修工单")
    @PostMapping
    public Result<RepTicket> createTicket(@Valid @RequestBody RepTicket ticket) {
        RepTicket created = ticketService.createTicket(ticket);
        return Result.success("工单创建成功", created);
    }

    @Operation(summary = "分页查询工单", description = "根据条件分页查询工单列表")
    @GetMapping
    public Result<IPage<RepTicket>> getTicketPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "工单状态") @RequestParam(required = false) String status,
            @Parameter(description = "优先级") @RequestParam(required = false) String priority,
            @Parameter(description = "报修人姓名") @RequestParam(required = false) String requesterName) {

        Page<RepTicket> page = new Page<>(pageNum, pageSize);
        IPage<RepTicket> result = ticketService.getTicketPage(page, status, priority, requesterName);
        return Result.success(result);
    }

    @Operation(summary = "获取工单详情", description = "根据ID获取工单详情，包括状态日志和处理记录")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getTicketDetail(
            @Parameter(description = "工单ID") @PathVariable Long id) {

        RepTicket ticket = ticketService.getTicketById(id);
        List<RepTicketStatusLog> statusLogs = ticketService.getStatusLogs(id);
        List<RepTicketProcessLog> processLogs = ticketService.getProcessLogs(id);
        RepTicketEvaluation evaluation = ticketService.getEvaluation(id);

        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticket);
        result.put("statusLogs", statusLogs);
        result.put("processLogs", processLogs);
        result.put("evaluation", evaluation);

        return Result.success(result);
    }

    @Operation(summary = "更新工单状态", description = "更新工单的状态")
    @PatchMapping("/{id}/status")
    public Result<Void> updateTicketStatus(
            @Parameter(description = "工单ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam String status,
            @Parameter(description = "操作人姓名") @RequestParam String operatorName,
            @Parameter(description = "操作人ID") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "说明") @RequestParam(required = false) String comment) {

        ticketService.updateTicketStatus(id, status, operatorName, operatorId, comment);
        return Result.success("工单状态更新成功");
    }

    @Operation(summary = "分配工单", description = "将工单分配给维修人员")
    @PatchMapping("/{id}/assign")
    public Result<Void> assignTicket(
            @Parameter(description = "工单ID") @PathVariable Long id,
            @Parameter(description = "处理人姓名") @RequestParam String assignedTo,
            @Parameter(description = "处理人ID") @RequestParam Long assignedToId) {

        ticketService.assignTicket(id, assignedTo, assignedToId);
        return Result.success("工单分配成功");
    }

    @Operation(summary = "撤销工单", description = "撤销工单")
    @DeleteMapping("/{id}")
    public Result<Void> cancelTicket(
            @Parameter(description = "工单ID") @PathVariable Long id,
            @Parameter(description = "撤销人姓名") @RequestParam String cancelerName,
            @Parameter(description = "撤销原因") @RequestParam String cancelReason) {

        ticketService.cancelTicket(id, cancelerName, cancelReason);
        return Result.success("工单撤销成功");
    }

    @Operation(summary = "评价工单", description = "对已解决的工单进行评价")
    @PostMapping("/{id}/evaluate")
    public Result<Void> evaluateTicket(
            @Parameter(description = "工单ID") @PathVariable Long id,
            @Valid @RequestBody RepTicketEvaluation evaluation) {

        ticketService.evaluateTicket(id, evaluation);
        return Result.success("评价成功");
    }

    @Operation(summary = "获取状态日志", description = "获取工单的状态变更日志")
    @GetMapping("/{id}/logs")
    public Result<List<RepTicketStatusLog>> getStatusLogs(
            @Parameter(description = "工单ID") @PathVariable Long id) {

        List<RepTicketStatusLog> logs = ticketService.getStatusLogs(id);
        return Result.success(logs);
    }

    @Operation(summary = "获取处理记录", description = "获取工单的处理记录")
    @GetMapping("/{id}/process")
    public Result<List<RepTicketProcessLog>> getProcessLogs(
            @Parameter(description = "工单ID") @PathVariable Long id) {

        List<RepTicketProcessLog> logs = ticketService.getProcessLogs(id);
        return Result.success(logs);
    }
}
