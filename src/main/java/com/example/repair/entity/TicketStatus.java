package com.example.repair.entity;

/**
 * 工单状态枚举
 */
public enum TicketStatus {

    /**
     * 待审核
     */
    PENDING("PENDING", "待审核"),

    /**
     * 已创建（待分配）
     */
    OPEN("OPEN", "已创建"),

    /**
     * 已分配
     */
    ASSIGNED("ASSIGNED", "已分配"),

    /**
     * 处理中
     */
    IN_PROGRESS("IN_PROGRESS", "处理中"),

    /**
     * 已暂停
     */
    ON_HOLD("ON_HOLD", "已暂停"),

    /**
     * 已解决
     */
    RESOLVED("RESOLVED", "已解决"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED", "已关闭"),

    /**
     * 已撤销
     */
    CANCELLED("CANCELLED", "已撤销");

    private final String code;
    private final String description;

    TicketStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TicketStatus fromCode(String code) {
        for (TicketStatus status : TicketStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
