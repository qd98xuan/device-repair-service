package com.example.repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单处理过程记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rep_ticket_process_logs")
@Schema(description = "工单处理过程记录")
public class RepTicketProcessLog extends BaseEntity {

    @Schema(description = "工单ID")
    private Long ticketId;

    @Schema(description = "处理类型：REPAIR-维修，COMMENT-备注，TRANSFER-转单，PAUSE-暂停，RESUME-恢复")
    private String processType;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "处理内容")
    private String content;

    @Schema(description = "处理过程图片（JSON格式）")
    private String images;

    @Schema(description = "处理耗时（分钟）")
    private Integer durationMinutes;
}
