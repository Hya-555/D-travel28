package com.travel.service;

import com.travel.config.BusinessException;
import com.travel.dto.CancelRequest;
import com.travel.entity.*;
import com.travel.mapper.*;
import com.travel.service.impl.CancelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 取消/变更服务单元测试
 * 核心测试: 手续费比例计算（含边界值）、全额取消、参加者取消、责任人交接
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("取消变更服务 - 单元测试")
class CancelServiceTest {

    @Mock
    private ApplicationCancelMapper cancelMapper;
    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private ParticipantMapper participantMapper;
    @Mock
    private TourGroupMapper tourGroupMapper;

    @InjectMocks
    private CancelServiceImpl cancelService;

    private Application testApp;
    private TourGroup testGroup;

    @BeforeEach
    void setUp() {
        testApp = new Application();
        testApp.setApplicationId(1L);
        testApp.setGroupCode("TG001");
        testApp.setContactName("张三");
        testApp.setContactPhone("13800138000");
        testApp.setAdultCount(2);
        testApp.setChildCount(1);
        testApp.setTotalAmount(new BigDecimal("13000.00"));
        testApp.setPaidAmount(new BigDecimal("2600.00")); // 已付20%订金
        testApp.setStatus("DEPOSIT_PAID");

        testGroup = new TourGroup();
        testGroup.setGroupCode("TG001");
        testGroup.setRouteCode("RT001");
        testGroup.setMaxCapacity(30);
        testGroup.setCurrentCount(8);
        testGroup.setStatus("AVAILABLE");
    }

    // ======================== 手续费比例计算（通过 FULL_CANCEL 间接测试） ========================

    @Nested
    @DisplayName("取消手续费率 — 边界值")
    class CancellationFeeRateTests {

        @Test
        @DisplayName("距出发>30天 → 手续费=0%（全额退款）")
        void shouldCharge0PercentWhenMoreThan30Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(31));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            CancelRequest req = buildCancelReq("FULL_CANCEL");
            ApplicationCancel result = cancelService.cancel(req);

            // 已付2600，手续费2600×0%=0，退款2600
            assertEquals(0, new BigDecimal("0.00").compareTo(result.getHandlingFee()));
            assertEquals(0, new BigDecimal("2600.00").compareTo(result.getRefundAmount()));
        }

        @Test
        @DisplayName("距出发31天 → 手续费=0%（边界：刚好>30）")
        void boundaryAt31Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(31));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));
            assertEquals(0, BigDecimal.ZERO.compareTo(result.getHandlingFee()));
        }

        @Test
        @DisplayName("距出发30天 → 手续费=20%（边界：≥10）")
        void boundaryAt30Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(30));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));
            // 2600 × 20% = 520
            assertEquals(0, new BigDecimal("520.00").compareTo(result.getHandlingFee()));
            assertEquals(0, new BigDecimal("2080.00").compareTo(result.getRefundAmount()));
        }

        @Test
        @DisplayName("距出发10天 → 手续费=20%（边界：≥10）")
        void boundaryAt10Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(10));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));
            assertEquals(0, new BigDecimal("520.00").compareTo(result.getHandlingFee()));
        }

        @Test
        @DisplayName("距出发9天 → 手续费=50%（边界：≥1）")
        void boundaryAt9Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(9));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));
            // 2600 × 50% = 1300
            assertEquals(0, new BigDecimal("1300.00").compareTo(result.getHandlingFee()));
            assertEquals(0, new BigDecimal("1300.00").compareTo(result.getRefundAmount()));
        }

        @Test
        @DisplayName("距出发1天 → 手续费=50%（边界：≥1）")
        void boundaryAt1Day() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(1));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));
            assertEquals(0, new BigDecimal("1300.00").compareTo(result.getHandlingFee()));
        }

        @Test
        @DisplayName("出发当天 → 手续费=100%（不退）")
        void boundaryAt0Days() {
            testGroup.setDepartureDate(LocalDate.now());
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));
            // 2600 × 100% = 2600，退款0
            assertEquals(0, new BigDecimal("2600.00").compareTo(result.getHandlingFee()));
            assertEquals(0, new BigDecimal("0.00").compareTo(result.getRefundAmount()));
        }

        @Test
        @DisplayName("已出发（负天数）→ 手续费=100%")
        void afterDeparture() {
            testGroup.setDepartureDate(LocalDate.now().minusDays(1));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));
            assertEquals(0, new BigDecimal("2600.00").compareTo(result.getHandlingFee()));
            assertEquals(0, BigDecimal.ZERO.compareTo(result.getRefundAmount()));
        }
    }

    // ======================== 全额取消 ========================

    @Nested
    @DisplayName("全额取消")
    class FullCancelTests {

        @Test
        @DisplayName("全额取消成功 — 退款为已付金额减手续费")
        void shouldFullCancelAndRefund() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(31));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            ApplicationCancel result = cancelService.cancel(buildCancelReq("FULL_CANCEL"));

            assertEquals("FULL_CANCEL", result.getCancelType());
            assertEquals("CANCELLED", testApp.getStatus());
            // 退还 3 个名额
            assertEquals(5, testGroup.getCurrentCount()); // 8 - 3 = 5
        }

        @Test
        @DisplayName("全额取消满团 — 状态从FULL恢复为AVAILABLE")
        void shouldRestoreAvailableFromFull() {
            testGroup.setCurrentCount(30);
            testGroup.setStatus("FULL");
            testGroup.setDepartureDate(LocalDate.now().plusDays(60));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            cancelService.cancel(buildCancelReq("FULL_CANCEL"));

            assertEquals(27, testGroup.getCurrentCount()); // 30 - 3 = 27
            assertEquals("AVAILABLE", testGroup.getStatus());
        }
    }

    // ======================== 参加者取消 ========================

    @Nested
    @DisplayName("参加者取消 — 部分取消")
    class ParticipantRemoveTests {

        @Test
        @DisplayName("取消普通参加者成功")
        void shouldRemoveParticipant() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(31));
            Participant participant = buildParticipant(100L, "李四", false);
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(participantMapper.selectById(100L)).thenReturn(participant);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(participantMapper.updateById(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            CancelRequest req = buildCancelReq("PARTICIPANT_REMOVE");
            req.setParticipantId(100L);
            ApplicationCancel result = cancelService.cancel(req);

            assertEquals("PARTICIPANT_REMOVE", result.getCancelType());
            // 每人已付 2600/3 ≈ 866.67, 手续费=0, 退款≈866.67
            assertTrue(result.getRefundAmount().compareTo(BigDecimal.ZERO) > 0);
            assertEquals("CANCELLED", participant.getStatus());
            assertEquals(7, testGroup.getCurrentCount()); // 8 - 1 = 7
        }

        @Test
        @DisplayName("取消责任人 — 必须指定新责任人")
        void shouldRequireNewContactPerson() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(31));
            Participant contactPerson = buildParticipant(100L, "张三", true);
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(participantMapper.selectById(100L)).thenReturn(contactPerson);

            CancelRequest req = buildCancelReq("PARTICIPANT_REMOVE");
            req.setParticipantId(100L);
            req.setNewContactParticipantId(null); // 未指定新责任人

            assertThrows(BusinessException.class, () -> cancelService.cancel(req));
        }

        @Test
        @DisplayName("取消责任人 — 成功交接给新责任人")
        void shouldTransferContactPerson() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(60));
            Participant oldContact = buildParticipant(100L, "张三", true);
            Participant newContact = buildParticipant(200L, "李四", false);
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(participantMapper.selectById(100L)).thenReturn(oldContact);
            when(participantMapper.selectById(200L)).thenReturn(newContact);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(participantMapper.updateById(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            CancelRequest req = buildCancelReq("PARTICIPANT_REMOVE");
            req.setParticipantId(100L);
            req.setNewContactParticipantId(200L);
            ApplicationCancel result = cancelService.cancel(req);

            // 验证责任人交接
            assertEquals("李四", testApp.getContactName());
            assertEquals("李四", result.getNewContactName());
            assertEquals("CANCELLED", oldContact.getStatus());
        }

        @Test
        @DisplayName("取消参加者 — 10~30天，手续费20%")
        void participantFeeAt10To30Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(15));
            Participant participant = buildParticipant(300L, "王五", false);
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(participantMapper.selectById(300L)).thenReturn(participant);
            when(cancelMapper.insert(any())).thenReturn(1);
            when(participantMapper.updateById(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            CancelRequest req = buildCancelReq("PARTICIPANT_REMOVE");
            req.setParticipantId(300L);
            ApplicationCancel result = cancelService.cancel(req);

            // 每人已付: 2600/3 ≈ 866.67, 手续费20%: ≈ 173.33, 退款: ≈ 693.34
            assertEquals(0, new BigDecimal("173.33").compareTo(result.getHandlingFee()));
            assertEquals(0, new BigDecimal("693.34").compareTo(result.getRefundAmount()));
        }
    }

    // ======================== 异常场景 ========================

    @Nested
    @DisplayName("取消异常场景")
    class CancelErrorTests {

        @Test
        @DisplayName("异常 — 申请不存在")
        void shouldThrowWhenAppNotFound() {
            when(applicationMapper.selectById(999L)).thenReturn(null);
            CancelRequest req = buildCancelReq("FULL_CANCEL");
            req.setApplicationId(999L);

            assertThrows(BusinessException.class, () -> cancelService.cancel(req));
        }

        @Test
        @DisplayName("异常 — 已取消的申请不可重复取消")
        void shouldThrowWhenAlreadyCancelled() {
            testApp.setStatus("CANCELLED");
            when(applicationMapper.selectById(1L)).thenReturn(testApp);

            assertThrows(BusinessException.class, () -> cancelService.cancel(buildCancelReq("FULL_CANCEL")));
        }

        @Test
        @DisplayName("异常 — 无效的取消类型")
        void shouldThrowOnInvalidCancelType() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(60));
            when(applicationMapper.selectById(1L)).thenReturn(testApp);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);

            CancelRequest req = buildCancelReq("INVALID_TYPE");
            assertThrows(BusinessException.class, () -> cancelService.cancel(req));
        }
    }

    // ======================== 辅助方法 ========================

    private CancelRequest buildCancelReq(String type) {
        CancelRequest req = new CancelRequest();
        req.setApplicationId(1L);
        req.setCancelType(type);
        req.setReason("测试取消");
        req.setEmployeeId(1001L);
        return req;
    }

    private Participant buildParticipant(Long id, String name, boolean isContact) {
        Participant p = new Participant();
        p.setParticipantId(id);
        p.setName(name);
        p.setPhone("13800000000");
        p.setIsContactPerson(isContact ? 1 : 0);
        p.setStatus("ACTIVE");
        return p;
    }
}
