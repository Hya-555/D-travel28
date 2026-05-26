package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("financial_export")
public class FinancialExport {
    @TableId(type = IdType.AUTO)
    private Long exportId;
    private LocalDate exportDate;
    private String dataType;
    private String content;
    private LocalDateTime exportTime;
}
