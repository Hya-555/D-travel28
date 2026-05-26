package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long paymentId;
    private Long applicationId;
    private String paymentNo;
    private String paymentType;
    private BigDecimal amount;
    private LocalDateTime payTime;
    private String status;
    private Long receivedBy;
}
