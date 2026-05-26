package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tour_price")
public class TourPrice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupCode;
    private BigDecimal adultPrice;
    private BigDecimal childPrice;
    private String discountDesc;
    private Integer isPublished;
    private LocalDateTime setTime;
    private Long setBy;
}
