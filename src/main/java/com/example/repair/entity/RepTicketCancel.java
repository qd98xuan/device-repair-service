package com.example.repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工单撤销记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rep_ticket_cancels")
@Schema(description = "工单撤销记录")
public class RepTicketCancel extends BaseEntity {

    @Schema(description = "工单ID")
    private Long ticketId;

    @Schema(description = "撤销人姓名")
    private String cancelerName;

    @Schema(description = "撤销原因")
    private String cancelReason;

    @Schema(description = "撤销时间")
    private LocalDateTime cancelTime;
}
