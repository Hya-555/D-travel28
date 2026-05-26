package com.travel.service.impl;

import cn.hutool.json.JSONUtil;
import com.travel.entity.FinancialExport;
import com.travel.entity.Payment;
import com.travel.mapper.FinancialExportMapper;
import com.travel.mapper.PaymentMapper;
import com.travel.service.FinancialExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialExportServiceImpl implements FinancialExportService {

    private final FinancialExportMapper exportMapper;
    private final PaymentMapper paymentMapper;

    @Override
    @Scheduled(cron = "0 0 23 * * ?")
    public void dailyExport() {
        LocalDate today = LocalDate.now();
        log.info("开始导出 {} 的财务数据...", today);

        List<Payment> todayPayments = paymentMapper.findByDate(today);

        if (todayPayments.isEmpty()) {
            log.info("今日无支付数据");
            return;
        }

        // 导出订金数据
        List<Payment> deposits = todayPayments.stream()
                .filter(p -> "DEPOSIT".equals(p.getPaymentType()))
                .toList();
        if (!deposits.isEmpty()) {
            FinancialExport export = new FinancialExport();
            export.setExportDate(today);
            export.setDataType("DEPOSIT");
            export.setContent(JSONUtil.toJsonStr(deposits));
            export.setExportTime(LocalDateTime.now());
            exportMapper.insert(export);
            log.info("导出订金数据 {} 条", deposits.size());
        }

        // 导出余款数据
        List<Payment> balances = todayPayments.stream()
                .filter(p -> "BALANCE".equals(p.getPaymentType()))
                .toList();
        if (!balances.isEmpty()) {
            FinancialExport export = new FinancialExport();
            export.setExportDate(today);
            export.setDataType("BALANCE");
            export.setContent(JSONUtil.toJsonStr(balances));
            export.setExportTime(LocalDateTime.now());
            exportMapper.insert(export);
            log.info("导出余款数据 {} 条", balances.size());
        }

        log.info("财务数据导出完成");
    }
}
