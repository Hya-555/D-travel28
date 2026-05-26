package com.travel.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceDTO {
    private String groupCode;
    private BigDecimal adultPrice;
    private BigDecimal childPrice;
    private String discountDesc;
    private Long employeeId;
}
