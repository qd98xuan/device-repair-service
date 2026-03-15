package com.example.repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工单评价实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rep_ticket_evaluations")
@Schema(description = "工单评价")
public class RepTicketEvaluation extends BaseEntity {

    @Schema(description = "工单ID")
    private Long ticketId;

    @Schema(description = "评价人姓名")
    private String evaluatorName;

    @Schema(description = "总体评分（1-5分）")
    private Integer overallScore;

    @Schema(description = "响应速度评分（1-5分）")
    private Integer responseSpeedScore;

    @Schema(description = "服务态度评分（1-5分）")
    private Integer serviceAttitudeScore;

    @Schema(description = "技术水平评分（1-5分）")
    private Integer technicalLevelScore;

    @Schema(description = "评价内容")
    private String comment;

    @Schema(description = "回复内容")
    private String replyContent;

    @Schema(description = "回复时间")
    private LocalDateTime repliedAt;
}
