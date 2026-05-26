package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("route_history")
public class RouteHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String routeCode;
    private String changeType;
    private String oldValue;
    private String newValue;
    private String changeReason;
    private LocalDateTime changeTime;
    private Long operatorId;
}
