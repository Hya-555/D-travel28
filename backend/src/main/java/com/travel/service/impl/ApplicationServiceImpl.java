package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.config.BusinessException;
import com.travel.dto.ApplyRequest;
import com.travel.dto.ParticipantDTO;
import com.travel.entity.*;
import com.travel.mapper.*;
import com.travel.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final TourGroupMapper tourGroupMapper;
    private final TourPriceMapper tourPriceMapper;
    private final ParticipantMapper participantMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public boolean checkAvailable(String groupCode) {
        TourGroup group = tourGroupMapper.selectById(groupCode);
        if (group == null) {
            throw new BusinessException("旅游团不存在");
        }
        if (!"AVAILABLE".equals(group.getStatus())) {
            return false;
        }
        if (LocalDate.now().isAfter(group.getDeadline())) {
            return false;
        }
        return group.getCurrentCount() < group.getMaxCapacity();
    }

    @Override
    @Transactional
    public Application apply(ApplyRequest req) {
        // 校验旅游团
        TourGroup group = tourGroupMapper.selectById(req.getGroupCode());
        if (group == null) {
            throw new BusinessException("旅游团不存在");
        }
        if (LocalDate.now().isAfter(group.getDeadline())) {
            throw new BusinessException("该旅游团已过截止日期，无法申请");
        }
        if (group.getCurrentCount() >= group.getMaxCapacity()) {
            throw new BusinessException("该旅游团人数已满，无法申请");
        }

        // 获取最新价格
        TourPrice price = tourPriceMapper.selectOne(new LambdaQueryWrapper<TourPrice>()
                .eq(TourPrice::getGroupCode, req.getGroupCode())
                .eq(TourPrice::getIsPublished, 1)
                .orderByDesc(TourPrice::getSetTime)
                .last("LIMIT 1"));
        if (price == null) {
            throw new BusinessException("该旅游团尚未公开价格");
        }

        // 计算总价
        BigDecimal adultTotal = price.getAdultPrice().multiply(BigDecimal.valueOf(req.getAdultCount()));
        BigDecimal childTotal = price.getChildPrice().multiply(BigDecimal.valueOf(req.getChildCount()));
        BigDecimal totalAmount = adultTotal.add(childTotal);

        // 计算订金: 根据距出发日期的天数
        long daysToDeparture = ChronoUnit.DAYS.between(LocalDate.now(), group.getDepartureDate());
        BigDecimal depositRatio;
        if (daysToDeparture >= 60) {
            depositRatio = new BigDecimal("0.10");
        } else if (daysToDeparture >= 30) {
            depositRatio = new BigDecimal("0.20");
        } else {
            depositRatio = BigDecimal.ONE; // 全款
        }
        BigDecimal depositAmount = totalAmount.multiply(depositRatio);

        // 创建申请
        Application app = new Application();
        app.setGroupCode(req.getGroupCode());
        app.setDepartureDate(group.getDepartureDate());
        app.setContactName(req.getContactName());
        app.setContactPhone(req.getContactPhone());
        app.setAdultCount(req.getAdultCount());
        app.setChildCount(req.getChildCount());
        app.setDepositAmount(depositAmount);
        app.setTotalAmount(totalAmount);
        app.setPaidAmount(BigDecimal.ZERO);
        app.setStatus("DRAFT");
        app.setApplyTime(LocalDateTime.now());
        app.setHandledBy(req.getEmployeeId());
        applicationMapper.insert(app);

        // 更新旅游团当前人数
        int newCount = group.getCurrentCount() + req.getAdultCount() + req.getChildCount();
        group.setCurrentCount(newCount);
        if (newCount >= group.getMaxCapacity()) {
            group.setStatus("FULL");
        }
        tourGroupMapper.updateById(group);

        return app;
    }

    @Override
    @Transactional
    public void payDeposit(Long applicationId, Long employeeId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"DRAFT".equals(app.getStatus())) {
            throw new BusinessException("当前状态不可支付订金");
        }

        // 创建支付记录
        Payment payment = new Payment();
        payment.setApplicationId(applicationId);
        payment.setPaymentNo("DEP" + applicationId + System.currentTimeMillis() % 100000);
        payment.setPaymentType("DEPOSIT");
        payment.setAmount(app.getDepositAmount());
        payment.setPayTime(LocalDateTime.now());
        payment.setReceivedBy(employeeId);
        paymentMapper.insert(payment);

        // 更新申请状态
        app.setPaidAmount(app.getDepositAmount());
        app.setStatus("DEPOSIT_PAID");
        applicationMapper.updateById(app);
    }

    @Override
    @Transactional
    public void addParticipant(Long applicationId, ParticipantDTO dto) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }

        Participant p = new Participant();
        p.setApplicationId(applicationId);
        p.setName(dto.getName());
        p.setGender(dto.getGender());
        p.setBirthDate(dto.getBirthDate());
        p.setPhone(dto.getPhone());
        p.setAddress(dto.getAddress());
        p.setZipCode(dto.getZipCode());
        p.setEmail(dto.getEmail());
        p.setEmergencyContact(dto.getEmergencyContact());
        p.setEmergencyAddress(dto.getEmergencyAddress());
        p.setEmergencyPhone(dto.getEmergencyPhone());
        p.setRelationship(dto.getRelationship());
        p.setIsContactPerson(Boolean.TRUE.equals(dto.getIsContactPerson()) ? 1 : 0);
        p.setStatus("ACTIVE");
        participantMapper.insert(p);
    }

    @Override
    @Transactional
    public void completeApplication(Long applicationId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        app.setStatus("COMPLETED");
        app.setCompleteTime(LocalDateTime.now());
        applicationMapper.updateById(app);
    }

    @Override
    public Application findByGroupAndContact(String groupCode, String departureDate, String contactName) {
        LocalDate depDate = LocalDate.parse(departureDate);
        return applicationMapper.selectOne(new LambdaQueryWrapper<Application>()
                .eq(Application::getGroupCode, groupCode)
                .eq(Application::getDepartureDate, depDate)
                .eq(Application::getContactName, contactName));
    }
}
