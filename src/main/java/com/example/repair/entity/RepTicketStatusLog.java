package com.example.repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单状态流转日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "rep_ticket_status_logs", excludeProperty = "updatedAt")
@Schema(description = "工单状态变更日志")
public class RepTicketStatusLog extends BaseEntity {

    @Schema(description = "工单ID")
    private Long ticketId;

    @Schema(description = "变更前状态")
    private String fromStatus;

    @Schema(description = "变更后状态")
    private String toStatus;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作动作")
    private String action;

    @Schema(description = "操作说明")
    private String comment;
}
