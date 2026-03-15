package com.example.repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报修工单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rep_tickets")
@Schema(description = "报修工单")
public class RepTicket extends BaseEntity {

    @Schema(description = "工单编号，格式：WO-年月日-序号")
    private String ticketNo;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "故障描述")
    private String description;

    @Schema(description = "故障类型：HARDWARE-硬件故障，SOFTWARE-软件故障，NETWORK-网络问题，OTHER-其他")
    private String faultType;

    @Schema(description = "优先级：LOW-低，MEDIUM-中，HIGH-高，URGENT-紧急")
    private String priority;

    @Schema(description = "工单状态")
    private String status;

    @Schema(description = "报修人姓名")
    private String requesterName;

    @Schema(description = "报修人电话")
    private String requesterPhone;

    @Schema(description = "报修人部门")
    private String requesterDept;

    @Schema(description = "故障位置")
    private String location;

    @Schema(description = "处理人姓名")
    private String assignedTo;

    @Schema(description = "处理人ID")
    private Long assignedToId;

    @Schema(description = "预计完成时间")
    private LocalDateTime estimatedCompletionTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际完成时间")
    private LocalDateTime actualCompletionTime;

    @Schema(description = "维修费用")
    private BigDecimal cost;

    @Schema(description = "故障图片列表（JSON格式）")
    private String imageUrls;

    @Schema(description = "附件列表（JSON格式）")
    private String attachmentUrls;

    @Schema(description = "是否SLA超时：0-否，1-是")
    private Integer isSlaBreached;

    @Schema(description = "关闭人ID")
    private Long closedBy;

    @Schema(description = "关闭时间")
    private LocalDateTime closedTime;

    @Schema(description = "关闭说明")
    private String closeComment;

    @Schema(description = "是否已评价：0-否，1-是")
    private Integer isEvaluated;
}
