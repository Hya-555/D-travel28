package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("application_cancel")
public class ApplicationCancel {
    @TableId(type = IdType.AUTO)
    private Long cancelId;
    private Long applicationId;
    private Long participantId;
    private String cancelType;
    private String reason;
    private BigDecimal handlingFee;
    private BigDecimal refundAmount;
    private String newContactName;
    private Long newContactParticipantId;
    private LocalDateTime cancelTime;
    private Long handledBy;
}
