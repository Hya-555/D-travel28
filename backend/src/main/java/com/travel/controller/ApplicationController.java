package com.travel.controller;

import com.travel.dto.*;
import com.travel.entity.Application;
import com.travel.entity.Participant;
import com.travel.mapper.ParticipantMapper;
import com.travel.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ParticipantMapper participantMapper;

    /** 检查旅游团是否可申请 */
    @GetMapping("/check/{groupCode}")
    public R<Boolean> checkAvailable(@PathVariable String groupCode) {
        return R.ok(applicationService.checkAvailable(groupCode));
    }

    /** 办理旅游申请 */
    @PostMapping("/apply")
    public R<Application> apply(@Valid @RequestBody ApplyRequest req) {
        return R.ok(applicationService.apply(req));
    }

    /** 支付订金 */
    @PostMapping("/deposit/{applicationId}")
    public R<Void> payDeposit(@PathVariable Long applicationId, @RequestParam Long employeeId) {
        applicationService.payDeposit(applicationId, employeeId);
        return R.ok();
    }

    /** 录入参加者 */
    @PostMapping("/{applicationId}/participant")
    public R<Void> addParticipant(@PathVariable Long applicationId, @RequestBody ParticipantDTO dto) {
        applicationService.addParticipant(applicationId, dto);
        return R.ok();
    }

    /** 查询申请下的参加者列表 */
    @GetMapping("/{applicationId}/participants")
    public R<List<Participant>> listParticipants(@PathVariable Long applicationId) {
        List<Participant> list = participantMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Participant>()
                        .eq(Participant::getApplicationId, applicationId));
        return R.ok(list);
    }

    /** 完成申请 */
    @PostMapping("/{applicationId}/complete")
    public R<Void> complete(@PathVariable Long applicationId) {
        applicationService.completeApplication(applicationId);
        return R.ok();
    }

    /** 根据条件查询申请 */
    @GetMapping("/find")
    public R<Application> findByGroupAndContact(@RequestParam String groupCode,
                                                 @RequestParam String departureDate,
                                                 @RequestParam String contactName) {
        return R.ok(applicationService.findByGroupAndContact(groupCode, departureDate, contactName));
    }

    /** 查询申请详情 */
    @GetMapping("/{applicationId}")
    public R<Application> detail(@PathVariable Long applicationId) {
        return R.ok(applicationService.getById(applicationId));
    }

    /** 申请列表 */
    @GetMapping("/list")
    public R<List<Application>> list() {
        return R.ok(applicationService.list());
    }
}
