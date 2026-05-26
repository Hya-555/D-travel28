package com.travel.service.impl;

import com.travel.config.BusinessException;
import com.travel.entity.Application;
import com.travel.entity.Receipt;
import com.travel.mapper.ApplicationMapper;
import com.travel.mapper.ReceiptMapper;
import com.travel.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptMapper receiptMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public Receipt printDepositReceipt(Long applicationId, Long employeeId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"DEPOSIT_PAID".equals(app.getStatus()) && !"COMPLETED".equals(app.getStatus())) {
            throw new BusinessException("当前状态不可打印收据");
        }

        Receipt receipt = new Receipt();
        receipt.setApplicationId(applicationId);
        receipt.setReceiptNo("RCP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        receipt.setReceiptType("DEPOSIT_RECEIPT");
        receipt.setPrintTime(LocalDateTime.now());
        receipt.setPrintedBy(employeeId);
        receiptMapper.insert(receipt);
        return receipt;
    }

    @Override
    @Transactional
    public List<Receipt> printDailyDocuments(Long employeeId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Application> completedApps = applicationMapper.findCompletedByDate(yesterday);

        List<Receipt> receipts = new ArrayList<>();
        for (Application app : completedApps) {
            boolean isFullPaid = app.getPaidAmount().compareTo(app.getTotalAmount()) >= 0;

            // 打印旅游确认书
            Receipt confirmation = new Receipt();
            confirmation.setApplicationId(app.getApplicationId());
            confirmation.setReceiptNo("CFM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            confirmation.setReceiptType("CONFIRMATION");
            confirmation.setPrintTime(LocalDateTime.now());
            confirmation.setPrintedBy(employeeId);
            receiptMapper.insert(confirmation);
            receipts.add(confirmation);

            // 未全款支付时，打印余额交款单
            if (!isFullPaid) {
                Receipt slip = new Receipt();
                slip.setApplicationId(app.getApplicationId());
                slip.setReceiptNo("SLP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                slip.setReceiptType("PAYMENT_SLIP");
                slip.setPrintTime(LocalDateTime.now());
                slip.setPrintedBy(employeeId);
                receiptMapper.insert(slip);
                receipts.add(slip);
            }
        }
        return receipts;
    }
}
