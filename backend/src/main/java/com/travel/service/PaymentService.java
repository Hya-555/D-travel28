package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.Payment;

public interface PaymentService extends IService<Payment> {

    /** 支付余款 */
    void payBalance(Long applicationId, String paymentNo, Long employeeId);

    /** 查询申请的所有支付记录 */
    java.util.List<Payment> findByApplicationId(Long applicationId);
}
