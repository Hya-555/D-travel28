package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tour_group")
public class TourGroup {
    @TableId
    private String groupCode;
    private String routeCode;
    private LocalDate departureDate;
    private LocalDate deadline;
    private Integer maxCapacity;
    private Integer currentCount;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
