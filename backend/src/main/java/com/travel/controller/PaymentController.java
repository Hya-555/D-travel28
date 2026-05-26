package com.travel.controller;

import com.travel.dto.PaymentRequest;
import com.travel.dto.R;
import com.travel.entity.Payment;
import com.travel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** 支付余款 */
    @PostMapping("/balance")
    public R<Void> payBalance(@RequestBody PaymentRequest req) {
        paymentService.payBalance(req.getApplicationId(), req.getPaymentNo(), req.getEmployeeId());
        return R.ok();
    }

    /** 查询申请的支付记录 */
    @GetMapping("/list/{applicationId}")
    public R<List<Payment>> listByApplication(@PathVariable Long applicationId) {
        return R.ok(paymentService.findByApplicationId(applicationId));
    }
}
