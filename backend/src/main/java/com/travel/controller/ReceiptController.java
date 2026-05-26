package com.travel.controller;

import com.travel.dto.R;
import com.travel.entity.Receipt;
import com.travel.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /** 打印订金收据 */
    @PostMapping("/deposit/{applicationId}")
    public R<Receipt> printDepositReceipt(@PathVariable Long applicationId, @RequestParam Long employeeId) {
        return R.ok(receiptService.printDepositReceipt(applicationId, employeeId));
    }

    /** 每日打印确认书和交款单 */
    @PostMapping("/daily")
    public R<List<Receipt>> printDaily(@RequestParam Long employeeId) {
        return R.ok(receiptService.printDailyDocuments(employeeId));
    }
}
