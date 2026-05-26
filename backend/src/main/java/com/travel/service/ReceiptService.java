package com.travel.service;

import com.travel.entity.Receipt;
import java.util.List;

public interface ReceiptService {

    /** 打印订金收据 */
    Receipt printDepositReceipt(Long applicationId, Long employeeId);

    /** 每日批量打印确认书和交款单 */
    List<Receipt> printDailyDocuments(Long employeeId);
}
