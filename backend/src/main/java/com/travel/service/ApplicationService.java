package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.*;
import com.travel.entity.Application;

public interface ApplicationService extends IService<Application> {

    /** 查询旅游团是否可申请 */
    boolean checkAvailable(String groupCode);

    /** 办理旅游申请（计算订金） */
    Application apply(ApplyRequest req);

    /** 支付订金 */
    void payDeposit(Long applicationId, Long employeeId);

    /** 录入参加者信息 */
    void addParticipant(Long applicationId, ParticipantDTO dto);

    /** 完成申请（所有参加者录入完毕） */
    void completeApplication(Long applicationId);

    /** 根据旅游团代码+出发日期+责任人查询申请 */
    Application findByGroupAndContact(String groupCode, String departureDate, String contactName);
}
