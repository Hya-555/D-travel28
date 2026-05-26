package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.config.BusinessException;
import com.travel.entity.Application;
import com.travel.entity.Payment;
import com.travel.mapper.ApplicationMapper;
import com.travel.mapper.PaymentMapper;
import com.travel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public void payBalance(Long applicationId, String paymentNo, Long employeeId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"COMPLETED".equals(app.getStatus())) {
            throw new BusinessException("申请未完成，无法支付余款");
        }

        // 计算余款
        BigDecimal balance = app.getTotalAmount().subtract(app.getPaidAmount());
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("余款已付清");
        }

        Payment payment = new Payment();
        payment.setApplicationId(applicationId);
        payment.setPaymentNo(paymentNo);
        payment.setPaymentType("BALANCE");
        payment.setAmount(balance);
        payment.setPayTime(LocalDateTime.now());
        payment.setReceivedBy(employeeId);
        paymentMapper.insert(payment);

        // 更新已付金额
        app.setPaidAmount(app.getTotalAmount());
        applicationMapper.updateById(app);
    }

    @Override
    public List<Payment> findByApplicationId(Long applicationId) {
        return paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getApplicationId, applicationId));
    }
}
