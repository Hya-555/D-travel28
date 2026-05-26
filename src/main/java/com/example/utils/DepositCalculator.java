package com.example.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DepositCalculator {
    // 计算订金金额
    public static double calculateDeposit(LocalDate startDate, double totalAmount) {
        LocalDate today = LocalDate.now();
        long daysBeforeStart = ChronoUnit.DAYS.between(today, startDate);

        if (daysBeforeStart >= 60) {
            // ≥2个月，订金10%
            return totalAmount * 0.1;
        } else if (daysBeforeStart >= 30) {
            // ≥1个月且<2个月，订金20%
            return totalAmount * 0.2;
        } else {
            // <1个月，付全款
            return totalAmount;
        }
    }

    // 计算取消手续费
    public static double calculateCancelFee(LocalDate startDate, double paidAmount) {
        LocalDate today = LocalDate.now();
        long daysBeforeStart = ChronoUnit.DAYS.between(today, startDate);

        if (daysBeforeStart > 30) {
            return 0; // 1个月以上，无手续费
        } else if (daysBeforeStart >= 10) {
            return paidAmount * 0.2; // 1个月到10天，扣20%
        } else if (daysBeforeStart >= 1) {
            return paidAmount * 0.5; // 10天到前1天，扣50%
        } else {
            return paidAmount; // 出发当天，扣全款
        }
    }
}