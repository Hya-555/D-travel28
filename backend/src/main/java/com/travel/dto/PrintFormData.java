package com.travel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 旅游申请书打印数据聚合 DTO
 * 聚合 Application + TourGroup + TourRoute + TourPrice 以及计算字段
 */
@Data
public class PrintFormData {
    // ==== 申请信息 (Application) ====
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

    // ==== 旅游团信息 (TourGroup) ====
    private String routeCode;
    private LocalDate deadline;

    // ==== 路线信息 (TourRoute) ====
    private String routeName;
    private String routeDescription;

    // ==== 价格信息 (TourPrice) ====
    private BigDecimal adultPrice;
    private BigDecimal childPrice;
    private String discountDesc;

    // ==== 计算字段 ====
    /** 成人小计 = adultCount * adultPrice */
    private BigDecimal adultSubtotal;
    /** 儿童小计 = childCount * childPrice */
    private BigDecimal childSubtotal;
    /** 应付余额 = totalAmount - paidAmount */
    private BigDecimal balanceDue;
}
