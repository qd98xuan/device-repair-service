package com.example.repair.mcp.controller;

import com.example.repair.common.BusinessException;
import com.example.repair.entity.RepTicket;
import com.example.repair.entity.RepTicketEvaluation;
import com.example.repair.mcp.dto.*;
import com.example.repair.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * MCP控制器
 * 提供符合MCP协议的接口，供AI Agent调用
 */
@Slf4j
@Tag(name = "MCP服务", description = "Model Context Protocol服务接口，供AI Agent调用")
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final TicketService ticketService;
    private final ObjectMapper objectMapper;
    @Value("${mcp.server.name:device-repair-mcp-server}")
    private String serverName;
    @Value("${mcp.server.version:1.0.0}")
    private String serverVersion;

    /**
     * 工具列表
     */
    private static final List<McpTool> TOOLS = new ArrayList<>();

    static {
        // 工具1: 创建报修工单
        Map<String, Object> createTicketSchema = new HashMap<>();
        createTicketSchema.put("type", "object");
        createTicketSchema.put("properties", Map.of(
                "title", Map.of("type", "string", "description", "工单标题"),
                "description", Map.of("type", "string", "description", "故障描述"),
                "faultType", Map.of("type", "string", "description", "故障类型：HARDWARE/SOFTWARE/NETWORK/OTHER"),
                "priority", Map.of("type", "string", "description", "优先级：LOW/MEDIUM/HIGH/URGENT", "default", "MEDIUM"),
                "requesterName", Map.of("type", "string", "description", "报修人姓名"),
                "requesterPhone", Map.of("type", "string", "description", "报修人电话"),
                "requesterDept", Map.of("type", "string", "description", "报修人部门"),
                "location", Map.of("type", "string", "description", "故障位置")
        ));
        createTicketSchema.put("required", List.of("title", "description", "requesterName"));

        TOOLS.add(new McpTool(
                "create_repair_ticket",
                "创建一个新的设备报修工单。用于报告设备故障并请求维修服务。",
                createTicketSchema
        ));

        // 工具2: 查询工单列表
        Map<String, Object> listTicketsSchema = new HashMap<>();
        listTicketsSchema.put("type", "object");
        listTicketsSchema.put("properties", Map.of(
                "status", Map.of("type", "string", "description", "工单状态：PENDING/OPEN/ASSIGNED/IN_PROGRESS/RESOLVED/CLOSED"),
                "priority", Map.of("type", "string", "description", "优先级：LOW/MEDIUM/HIGH/URGENT"),
                "requesterName", Map.of("type", "string", "description", "报修人姓名"),
                "pageNum", Map.of("type", "integer", "description", "页码", "default", 1),
                "pageSize", Map.of("type", "integer", "description", "每页条数", "default", 10)
        ));

        TOOLS.add(new McpTool(
                "list_repair_tickets",
                "查询报修工单列表。可以按状态、优先级、报修人等条件筛选。",
                listTicketsSchema
        ));

        // 工具3: 获取工单详情
        Map<String, Object> getTicketSchema = new HashMap<>();
        getTicketSchema.put("type", "object");
        getTicketSchema.put("properties", Map.of(
                "ticketId", Map.of("type", "integer", "description", "工单ID")
        ));
        getTicketSchema.put("required", List.of("ticketId"));

        TOOLS.add(new McpTool(
                "get_ticket_detail",
                "获取指定工单的详细信息，包括状态变更历史和处理记录。",
                getTicketSchema
        ));

        // 工具4: 更新工单状态
        Map<String, Object> updateStatusSchema = new HashMap<>();
        updateStatusSchema.put("type", "object");
        updateStatusSchema.put("properties", Map.of(
                "ticketId", Map.of("type", "integer", "description", "工单ID"),
                "status", Map.of("type", "string", "description", "新状态：OPEN/ASSIGNED/IN_PROGRESS/ON_HOLD/RESOLVED/CLOSED"),
                "operatorName", Map.of("type", "string", "description", "操作人姓名"),
                "operatorId", Map.of("type", "integer", "description", "操作人ID"),
                "comment", Map.of("type", "string", "description", "操作说明")
        ));
        updateStatusSchema.put("required", List.of("ticketId", "status", "operatorName"));

        TOOLS.add(new McpTool(
                "update_ticket_status",
                "更新报修工单的状态。用于维修人员处理工单、完成任务等操作。",
                updateStatusSchema
        ));

        // 工具5: 分配工单
        Map<String, Object> assignTicketSchema = new HashMap<>();
        assignTicketSchema.put("type", "object");
        assignTicketSchema.put("properties", Map.of(
                "ticketId", Map.of("type", "integer", "description", "工单ID"),
                "assignedTo", Map.of("type", "string", "description", "处理人姓名"),
                "assignedToId", Map.of("type", "integer", "description", "处理人ID")
        ));
        assignTicketSchema.put("required", List.of("ticketId", "assignedTo", "assignedToId"));

        TOOLS.add(new McpTool(
                "assign_ticket",
                "将工单分配给维修人员。",
                assignTicketSchema
        ));

        // 工具6: 撤销工单
        Map<String, Object> cancelTicketSchema = new HashMap<>();
        cancelTicketSchema.put("type", "object");
        cancelTicketSchema.put("properties", Map.of(
                "ticketId", Map.of("type", "integer", "description", "工单ID"),
                "cancelerName", Map.of("type", "string", "description", "撤销人姓名"),
                "cancelReason", Map.of("type", "string", "description", "撤销原因")
        ));
        cancelTicketSchema.put("required", List.of("ticketId", "cancelerName", "cancelReason"));

        TOOLS.add(new McpTool(
                "cancel_ticket",
                "撤销未处理完成的工单。",
                cancelTicketSchema
        ));

        // 工具7: 评价工单
        Map<String, Object> evaluateSchema = new HashMap<>();
        evaluateSchema.put("type", "object");
        evaluateSchema.put("properties", Map.of(
                "ticketId", Map.of("type", "integer", "description", "工单ID"),
                "evaluatorName", Map.of("type", "string", "description", "评价人姓名"),
                "overallScore", Map.of("type", "integer", "description", "总体评分（1-5分）"),
                "responseSpeedScore", Map.of("type", "integer", "description", "响应速度评分（1-5分）"),
                "serviceAttitudeScore", Map.of("type", "integer", "description", "服务态度评分（1-5分）"),
                "technicalLevelScore", Map.of("type", "integer", "description", "技术水平评分（1-5分）"),
                "comment", Map.of("type", "string", "description", "评价内容")
        ));
        evaluateSchema.put("required", List.of("ticketId", "evaluatorName", "overallScore"));

        TOOLS.add(new McpTool(
                "evaluate_ticket",
                "对已解决的报修工单进行评价。用于收集用户反馈。",
                evaluateSchema
        ));
    }

    @Operation(summary = "MCP消息处理", description = "处理 MCP 协议消息，支持 initialize、tools/list、tools/call，请求地址可使用 /mcp 或 /mcp/messages")
    @PostMapping({"", "/messages"})
    public McpMessage handleMessage(@RequestBody McpMessage message) {
        log.info("收到MCP消息: method={}", message.getMethod());

        try {
            // 处理工具列表请求
            if ("tools/list".equals(message.getMethod())) {
                return handleToolsList(message);
            }

            // 处理工具调用请求
            if ("tools/call".equals(message.getMethod())) {
                return handleToolsCall(message);
            }

            // 初始化请求
            if ("initialize".equals(message.getMethod())) {
                return handleInitialize(message);
            }

            // 未知方法
            McpMessage errorResponse = new McpMessage();
            errorResponse.setId(message.getId());
            errorResponse.setError(new McpError(McpError.METHOD_NOT_FOUND, "Method not found: " + message.getMethod()));
            return errorResponse;

        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getMessage());
            McpMessage errorResponse = new McpMessage();
            errorResponse.setId(message.getId());
            errorResponse.setError(new McpError(McpError.INTERNAL_ERROR, e.getMessage()));
            return errorResponse;
        } catch (Exception e) {
            log.error("系统异常", e);
            McpMessage errorResponse = new McpMessage();
            errorResponse.setId(message.getId());
            errorResponse.setError(new McpError(McpError.INTERNAL_ERROR, "Internal error: " + e.getMessage()));
            return errorResponse;
        }
    }

    /**
     * 处理工具列表请求
     */
    private McpMessage handleToolsList(McpMessage message) {
        McpMessage response = new McpMessage();
        response.setId(message.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("tools", TOOLS);
        response.setResult(result);

        return response;
    }

    /**
     * 处理初始化请求
     */
    private McpMessage handleInitialize(McpMessage message) {
        McpMessage response = new McpMessage();
        response.setId(message.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("capabilities", Map.of(
                "tools", Map.of()
        ));
        result.put("serverInfo", Map.of(
                "name", serverName,
                "version", serverVersion
        ));

        response.setResult(result);
        return response;
    }

    /**
     * 处理工具调用请求
     */
    @SuppressWarnings("unchecked")
    private McpMessage handleToolsCall(McpMessage message) {
        Map<String, Object> params = message.getParams();
        if (params == null) {
            McpMessage errorResponse = new McpMessage();
            errorResponse.setId(message.getId());
            errorResponse.setError(new McpError(McpError.INVALID_PARAMS, "Missing params"));
            return errorResponse;
        }

        String toolName = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        if (toolName == null) {
            McpMessage errorResponse = new McpMessage();
            errorResponse.setId(message.getId());
            errorResponse.setError(new McpError(McpError.INVALID_PARAMS, "Missing tool name"));
            return errorResponse;
        }

        log.info("调用工具: {}, 参数: {}", toolName, arguments);

        McpToolCallResult result;
        switch (toolName) {
            case "create_repair_ticket":
                result = createRepairTicket(arguments);
                break;
            case "list_repair_tickets":
                result = listRepairTickets(arguments);
                break;
            case "get_ticket_detail":
                result = getTicketDetail(arguments);
                break;
            case "update_ticket_status":
                result = updateTicketStatus(arguments);
                break;
            case "assign_ticket":
                result = assignTicket(arguments);
                break;
            case "cancel_ticket":
                result = cancelTicket(arguments);
                break;
            case "evaluate_ticket":
                result = evaluateTicket(arguments);
                break;
            default:
                McpMessage errorResponse = new McpMessage();
                errorResponse.setId(message.getId());
                errorResponse.setError(new McpError(McpError.METHOD_NOT_FOUND, "Unknown tool: " + toolName));
                return errorResponse;
        }

        McpMessage response = new McpMessage();
        response.setId(message.getId());
        response.setResult(result);
        return response;
    }

    /**
     * 创建报修工单
     */
    @SuppressWarnings("unchecked")
    private McpToolCallResult createRepairTicket(Map<String, Object> arguments) {
        RepTicket ticket = new RepTicket();
        ticket.setTitle((String) arguments.get("title"));
        ticket.setDescription((String) arguments.get("description"));
        ticket.setFaultType((String) arguments.getOrDefault("faultType", "OTHER"));
        ticket.setPriority((String) arguments.getOrDefault("priority", "MEDIUM"));
        ticket.setRequesterName((String) arguments.get("requesterName"));
        ticket.setRequesterPhone((String) arguments.get("requesterPhone"));
        ticket.setRequesterDept((String) arguments.get("requesterDept"));
        ticket.setLocation((String) arguments.get("location"));

        RepTicket created = ticketService.createTicket(ticket);

        String message = String.format(
                "工单创建成功！\n工单编号：%s\n工单标题：%s\n状态：%s\n报修人：%s\n创建时间：%s",
                created.getTicketNo(),
                created.getTitle(),
                created.getStatus(),
                created.getRequesterName(),
                created.getCreatedAt()
        );

        return McpToolCallResult.text(message);
    }

    /**
     * 查询工单列表
     */
    @SuppressWarnings("unchecked")
    private McpToolCallResult listRepairTickets(Map<String, Object> arguments) {
        String status = (String) arguments.get("status");
        String priority = (String) arguments.get("priority");
        String requesterName = (String) arguments.get("requesterName");
        int pageNum = arguments.get("pageNum") != null ? (Integer) arguments.get("pageNum") : 1;
        int pageSize = arguments.get("pageSize") != null ? (Integer) arguments.get("pageSize") : 10;

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<RepTicket> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        var pageResult = ticketService.getTicketPage(page, status, priority, requesterName);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("共找到 %d 条工单记录\n\n", pageResult.getTotal()));

        for (RepTicket ticket : pageResult.getRecords()) {
            sb.append(String.format("ID: %d | 编号: %s | 标题: %s | 状态: %s | 优先级: %s | 报修人: %s\n",
                    ticket.getId(),
                    ticket.getTicketNo(),
                    ticket.getTitle(),
                    ticket.getStatus(),
                    ticket.getPriority(),
                    ticket.getRequesterName()));
        }

        return McpToolCallResult.text(sb.toString());
    }

    /**
     * 获取工单详情
     */
    @SuppressWarnings("unchecked")
    private McpToolCallResult getTicketDetail(Map<String, Object> arguments) {
        Long ticketId = ((Number) arguments.get("ticketId")).longValue();

        RepTicket ticket = ticketService.getTicketById(ticketId);
        var statusLogs = ticketService.getStatusLogs(ticketId);
        var processLogs = ticketService.getProcessLogs(ticketId);
        RepTicketEvaluation evaluation = ticketService.getEvaluation(ticketId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== 工单详情 ===\n");
        sb.append(String.format("工单编号: %s\n", ticket.getTicketNo()));
        sb.append(String.format("标题: %s\n", ticket.getTitle()));
        sb.append(String.format("描述: %s\n", ticket.getDescription()));
        sb.append(String.format("状态: %s\n", ticket.getStatus()));
        sb.append(String.format("优先级: %s\n", ticket.getPriority()));
        sb.append(String.format("故障类型: %s\n", ticket.getFaultType()));
        sb.append(String.format("报修人: %s\n", ticket.getRequesterName()));
        sb.append(String.format("联系电话: %s\n", ticket.getRequesterPhone()));
        sb.append(String.format("故障位置: %s\n", ticket.getLocation()));
        sb.append(String.format("处理人: %s\n", ticket.getAssignedTo() != null ? ticket.getAssignedTo() : "待分配"));
        sb.append(String.format("创建时间: %s\n", ticket.getCreatedAt()));

        if (ticket.getActualCompletionTime() != null) {
            sb.append(String.format("完成时间: %s\n", ticket.getActualCompletionTime()));
        }

        if (evaluation != null) {
            sb.append("\n=== 评价信息 ===\n");
            sb.append(String.format("总体评分: %d/5\n", evaluation.getOverallScore()));
            sb.append(String.format("评价内容: %s\n", evaluation.getComment()));
        }

        if (!statusLogs.isEmpty()) {
            sb.append("\n=== 状态变更历史 ===\n");
            for (var log : statusLogs) {
                sb.append(String.format("%s: %s -> %s (操作: %s)\n",
                        log.getCreatedAt(),
                        log.getFromStatus(),
                        log.getToStatus(),
                        log.getAction()));
            }
        }

        if (!processLogs.isEmpty()) {
            sb.append("\n=== 处理记录 ===\n");
            for (var log : processLogs) {
                sb.append(String.format("%s: [%s] %s - %s\n",
                        log.getCreatedAt(),
                        log.getProcessType(),
                        log.getOperatorName(),
                        log.getContent()));
            }
        }

        return McpToolCallResult.text(sb.toString());
    }

    /**
     * 更新工单状态
     */
    @SuppressWarnings("unchecked")
    private McpToolCallResult updateTicketStatus(Map<String, Object> arguments) {
        Long ticketId = ((Number) arguments.get("ticketId")).longValue();
        String status = (String) arguments.get("status");
        String operatorName = (String) arguments.get("operatorName");
        Long operatorId = arguments.get("operatorId") != null ? ((Number) arguments.get("operatorId")).longValue() : null;
        String comment = (String) arguments.get("comment");

        ticketService.updateTicketStatus(ticketId, status, operatorName, operatorId, comment);

        RepTicket ticket = ticketService.getTicketById(ticketId);
        String message = String.format(
                "工单状态更新成功！\n工单编号：%s\n新状态：%s\n操作人：%s",
                ticket.getTicketNo(),
                ticket.getStatus(),
                operatorName
        );

        return McpToolCallResult.text(message);
    }

    /**
     * 分配工单
     */
    @SuppressWarnings("unchecked")
    private McpToolCallResult assignTicket(Map<String, Object> arguments) {
        Long ticketId = ((Number) arguments.get("ticketId")).longValue();
        String assignedTo = (String) arguments.get("assignedTo");
        Long assignedToId = ((Number) arguments.get("assignedToId")).longValue();

        ticketService.assignTicket(ticketId, assignedTo, assignedToId);

        RepTicket ticket = ticketService.getTicketById(ticketId);
        String message = String.format(
                "工单分配成功！\n工单编号：%s\n处理人：%s\n当前状态：%s",
                ticket.getTicketNo(),
                assignedTo,
                ticket.getStatus()
        );

        return McpToolCallResult.text(message);
    }

    /**
     * 撤销工单
     */
    @SuppressWarnings("unchecked")
    private McpToolCallResult cancelTicket(Map<String, Object> arguments) {
        Long ticketId = ((Number) arguments.get("ticketId")).longValue();
        String cancelerName = (String) arguments.get("cancelerName");
        String cancelReason = (String) arguments.get("cancelReason");

        ticketService.cancelTicket(ticketId, cancelerName, cancelReason);

        RepTicket ticket = ticketService.getTicketById(ticketId);
        String message = String.format(
                "工单撤销成功！\n工单编号：%s\n当前状态：%s\n撤销人：%s",
                ticket.getTicketNo(),
                ticket.getStatus(),
                cancelerName
        );

        return McpToolCallResult.text(message);
    }

    /**
     * 评价工单
     */
    @SuppressWarnings("unchecked")
    private McpToolCallResult evaluateTicket(Map<String, Object> arguments) {
        Long ticketId = ((Number) arguments.get("ticketId")).longValue();

        RepTicketEvaluation evaluation = new RepTicketEvaluation();
        evaluation.setEvaluatorName((String) arguments.get("evaluatorName"));
        evaluation.setOverallScore(((Number) arguments.get("overallScore")).intValue());

        if (arguments.get("responseSpeedScore") != null) {
            evaluation.setResponseSpeedScore(((Number) arguments.get("responseSpeedScore")).intValue());
        }
        if (arguments.get("serviceAttitudeScore") != null) {
            evaluation.setServiceAttitudeScore(((Number) arguments.get("serviceAttitudeScore")).intValue());
        }
        if (arguments.get("technicalLevelScore") != null) {
            evaluation.setTechnicalLevelScore(((Number) arguments.get("technicalLevelScore")).intValue());
        }
        evaluation.setComment((String) arguments.get("comment"));

        ticketService.evaluateTicket(ticketId, evaluation);

        return McpToolCallResult.text("工单评价成功！感谢您的反馈。");
    }
}
