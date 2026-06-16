package com.travel.service;

import com.travel.dto.PrintFormData;
import com.travel.entity.Receipt;
import java.util.List;

public interface ReceiptService {

    /** 打印订金收据 */
    Receipt printDepositReceipt(Long applicationId, Long employeeId);

    /** 每日批量打印确认书和交款单 */
    List<Receipt> printDailyDocuments(Long employeeId);

    /** 获取打印旅游申请书所需的完整数据 */
    PrintFormData getPrintFormData(Long applicationId);
}
