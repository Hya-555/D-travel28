package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("receipt")
public class Receipt {
    @TableId(type = IdType.AUTO)
    private Long receiptId;
    private Long applicationId;
    private String receiptNo;
    private String receiptType;
    private LocalDateTime printTime;
    private Long printedBy;
}
