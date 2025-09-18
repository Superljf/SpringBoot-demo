package com.xkcoding.swagger.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 消息实体类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息实体")
public class MessageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "MSG001")
    private String messageId;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "这是一条测试消息")
    private String content;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型", example = "DIRECT")
    private String messageType;

    /**
     * 发送者
     */
    @Schema(description = "发送者", example = "system")
    private String sender;

    /**
     * 接收者
     */
    @Schema(description = "接收者", example = "user001")
    private String receiver;

    /**
     * 路由键
     */
    @Schema(description = "路由键", example = "user.email.send")
    private String routingKey;

    /**
     * 延迟时间（毫秒）
     */
    @Schema(description = "延迟时间（毫秒）", example = "5000")
    private Long delayTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 额外数据
     */
    @Schema(description = "额外数据")
    private Object extraData;

    /**
     * 构造器 - 用于简单消息
     */
    public MessageInfo(String content) {
        this.content = content;
        this.createTime = LocalDateTime.now();
        this.messageId = "MSG" + System.currentTimeMillis();
    }

    /**
     * 构造器 - 用于指定类型的消息
     */
    public MessageInfo(String content, String messageType) {
        this(content);
        this.messageType = messageType;
    }

    /**
     * 构造器 - 用于指定路由键的消息
     */
    public MessageInfo(String content, String messageType, String routingKey) {
        this(content, messageType);
        this.routingKey = routingKey;
    }

    /**
     * 构造器 - 用于延迟消息
     */
    public MessageInfo(String content, String messageType, Long delayTime) {
        this(content, messageType);
        this.delayTime = delayTime;
    }
}