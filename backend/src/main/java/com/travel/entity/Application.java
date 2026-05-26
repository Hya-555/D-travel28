package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("application")
public class Application {
    @TableId(type = IdType.AUTO)
    private Long applicationId;
    private String groupCode;
    private LocalDate departureDate;
    private String contactName;
    private String contactPhone;
    private Integer adultCount;
    private Integer childCount;
    private BigDecimal depositAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String status;
    private LocalDateTime applyTime;
    private LocalDateTime completeTime;
    private Long handledBy;
}
