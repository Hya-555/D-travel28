package com.travel.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long applicationId;
    private String paymentNo;
    private Long employeeId;
}
