package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.config.BusinessException;
import com.travel.dto.CancelRequest;
import com.travel.entity.*;
import com.travel.mapper.*;
import com.travel.service.CancelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CancelServiceImpl extends ServiceImpl<ApplicationCancelMapper, ApplicationCancel> implements CancelService {

    private final ApplicationCancelMapper cancelMapper;
    private final ApplicationMapper applicationMapper;
    private final ParticipantMapper participantMapper;
    private final TourGroupMapper tourGroupMapper;

    @Override
    @Transactional
    public ApplicationCancel cancel(CancelRequest req) {
        Application app = applicationMapper.selectById(req.getApplicationId());
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if ("CANCELLED".equals(app.getStatus())) {
            throw new BusinessException("该申请已取消");
        }

        TourGroup group = tourGroupMapper.selectById(app.getGroupCode());
        long daysToDeparture = ChronoUnit.DAYS.between(LocalDate.now(), group.getDepartureDate());

        ApplicationCancel cancel = new ApplicationCancel();
        cancel.setApplicationId(req.getApplicationId());
        cancel.setCancelTime(LocalDateTime.now());
        cancel.setReason(req.getReason());
        cancel.setHandledBy(req.getEmployeeId());

        if ("FULL_CANCEL".equals(req.getCancelType())) {
            return handleFullCancel(app, group, cancel, daysToDeparture);
        } else if ("PARTICIPANT_REMOVE".equals(req.getCancelType())) {
            return handleParticipantRemove(app, group, cancel, daysToDeparture,
                    req.getParticipantId(), req.getNewContactParticipantId());
        } else {
            throw new BusinessException("无效的取消类型");
        }
    }

    private ApplicationCancel handleFullCancel(Application app, TourGroup group,
                                                ApplicationCancel cancel, long daysToDeparture) {
        // 计算手续费
        BigDecimal feeRate = calcHandlingFeeRate(daysToDeparture);
        BigDecimal handlingFee = app.getPaidAmount().multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal refund = app.getPaidAmount().subtract(handlingFee);

        cancel.setCancelType("FULL_CANCEL");
        cancel.setHandlingFee(handlingFee);
        cancel.setRefundAmount(refund);
        cancelMapper.insert(cancel);

        // 退还参加者名额
        int participantCount = (app.getAdultCount() != null ? app.getAdultCount() : 0)
                + (app.getChildCount() != null ? app.getChildCount() : 0);
        group.setCurrentCount(Math.max(0, group.getCurrentCount() - participantCount));
        if (group.getCurrentCount() < group.getMaxCapacity()) {
            group.setStatus("AVAILABLE");
        }
        tourGroupMapper.updateById(group);

        app.setStatus("CANCELLED");
        applicationMapper.updateById(app);

        return cancel;
    }

    private ApplicationCancel handleParticipantRemove(Application app, TourGroup group,
                                                       ApplicationCancel cancel, long daysToDeparture,
                                                       Long participantId, Long newContactParticipantId) {
        Participant participant = participantMapper.selectById(participantId);
        if (participant == null) {
            throw new BusinessException("参加者不存在");
        }

        // 如果被取消的是责任人，必须指定新责任人
        if (participant.getIsContactPerson() != null && participant.getIsContactPerson() == 1) {
            if (newContactParticipantId == null) {
                throw new BusinessException("申请责任人被取消，必须选定新的申请责任人");
            }
            Participant newContact = participantMapper.selectById(newContactParticipantId);
            if (newContact == null) {
                throw new BusinessException("新责任人不存在");
            }
            // 更新申请的责任人信息
            app.setContactName(newContact.getName());
            app.setContactPhone(newContact.getPhone());
            applicationMapper.updateById(app);

            cancel.setNewContactName(newContact.getName());
            cancel.setNewContactParticipantId(newContactParticipantId);

            // 更新新责任人的标志
            participant.setIsContactPerson(0);
            participantMapper.updateById(participant);
            newContact.setIsContactPerson(1);
            participantMapper.updateById(newContact);
        }

        // 按比例计算手续费（单人费用比例）
        int totalPeople = app.getAdultCount() + app.getChildCount();
        BigDecimal perPersonPaid = totalPeople > 0
                ? app.getPaidAmount().divide(BigDecimal.valueOf(totalPeople), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal feeRate = calcHandlingFeeRate(daysToDeparture);
        BigDecimal handlingFee = perPersonPaid.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal refund = perPersonPaid.subtract(handlingFee);

        cancel.setCancelType("PARTICIPANT_REMOVE");
        cancel.setParticipantId(participantId);
        cancel.setHandlingFee(handlingFee);
        cancel.setRefundAmount(refund);
        cancelMapper.insert(cancel);

        // 更新参加者状态
        participant.setStatus("CANCELLED");
        participantMapper.updateById(participant);

        // 退还名额
        group.setCurrentCount(Math.max(0, group.getCurrentCount() - 1));
        if (group.getCurrentCount() < group.getMaxCapacity()) {
            group.setStatus("AVAILABLE");
        }
        tourGroupMapper.updateById(group);

        return cancel;
    }

    /**
     * 计算取消手续费比例
     * 表3: >30天→0%, 10~30天→20%, 1~9天→50%, 0天→100%
     */
    private BigDecimal calcHandlingFeeRate(long daysToDeparture) {
        if (daysToDeparture > 30) {
            return BigDecimal.ZERO;
        } else if (daysToDeparture >= 10) {
            return new BigDecimal("0.20");
        } else if (daysToDeparture >= 1) {
            return new BigDecimal("0.50");
        } else {
            return BigDecimal.ONE;
        }
    }
}
