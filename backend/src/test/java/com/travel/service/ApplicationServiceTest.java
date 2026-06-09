package com.travel.service;

import com.travel.config.BusinessException;
import com.travel.dto.ApplyRequest;
import com.travel.entity.Application;
import com.travel.entity.TourGroup;
import com.travel.entity.TourPrice;
import com.travel.mapper.*;
import com.travel.service.impl.ApplicationServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 旅游申请服务单元测试
 * 核心测试: 订金比例计算、人数上限、截止日期校验、状态流转
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("旅游申请服务 - 单元测试")
class ApplicationServiceTest {

    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private TourGroupMapper tourGroupMapper;
    @Mock
    private TourPriceMapper tourPriceMapper;
    @Mock
    private ParticipantMapper participantMapper;
    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private TourGroup testGroup;
    private TourPrice testPrice;

    @BeforeEach
    void setUp() {
        testGroup = new TourGroup();
        testGroup.setGroupCode("TG001");
        testGroup.setRouteCode("RT001");
        testGroup.setMaxCapacity(30);
        testGroup.setCurrentCount(5);
        testGroup.setStatus("AVAILABLE");
        testGroup.setDeadline(LocalDate.now().plusDays(60));

        testPrice = new TourPrice();
        testPrice.setGroupCode("TG001");
        testPrice.setAdultPrice(new BigDecimal("5000.00"));
        testPrice.setChildPrice(new BigDecimal("3000.00"));
        testPrice.setIsPublished(1);
    }

    // ======================== 可用性检查 ========================

    @Nested
    @DisplayName("旅游团可用性检查")
    class CheckAvailableTests {

        @Test
        @DisplayName("正常可用 — 人数未满 + 未过截止日期")
        void shouldReturnTrueWhenAvailable() {
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);

            boolean result = applicationService.checkAvailable("TG001");

            assertTrue(result);
        }

        @Test
        @DisplayName("不可用 — 人数已满")
        void shouldReturnFalseWhenFull() {
            testGroup.setCurrentCount(30);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);

            boolean result = applicationService.checkAvailable("TG001");

            assertFalse(result);
        }

        @Test
        @DisplayName("不可用 — 已过截止日期")
        void shouldReturnFalseWhenPastDeadline() {
            testGroup.setDeadline(LocalDate.now().minusDays(1));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);

            boolean result = applicationService.checkAvailable("TG001");

            assertFalse(result);
        }

        @Test
        @DisplayName("不可用 — 状态不为AVAILABLE")
        void shouldReturnFalseWhenNotAvailable() {
            testGroup.setStatus("CLOSED");
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);

            boolean result = applicationService.checkAvailable("TG001");

            assertFalse(result);
        }

        @Test
        @DisplayName("异常 — 旅游团不存在")
        void shouldThrowWhenGroupNotFound() {
            when(tourGroupMapper.selectById("TG999")).thenReturn(null);

            assertThrows(BusinessException.class, () -> applicationService.checkAvailable("TG999"));
        }
    }

    // ======================== 申请 & 订金计算 ========================

    @Nested
    @DisplayName("旅游申请 — 订金比例计算")
    class ApplyDepositTests {

        @Test
        @DisplayName("距出发≥60天 → 订金=总价×10%")
        void shouldCharge10PercentWhen60DaysOrMore() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(60));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            ApplyRequest req = buildRequest(2, 1);
            Application app = applicationService.apply(req);

            // 总价 = 5000×2 + 3000×1 = 13000, 订金 = 13000 × 0.10 = 1300
            assertEquals(0, new BigDecimal("13000.00").compareTo(app.getTotalAmount()));
            assertEquals(0, new BigDecimal("1300.00").compareTo(app.getDepositAmount()));
        }

        @Test
        @DisplayName("距出发30~59天 → 订金=总价×20%")
        void shouldCharge20PercentWhen30To59Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(45));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            ApplyRequest req = buildRequest(2, 1);
            Application app = applicationService.apply(req);

            // 总价 = 13000, 订金 = 13000 × 0.20 = 2600
            assertEquals(0, new BigDecimal("2600.00").compareTo(app.getDepositAmount()));
        }

        @Test
        @DisplayName("距出发29天 → 订金=全款100%（边界：<30天）")
        void shouldCharge100PercentAt29Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(29));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            ApplyRequest req = buildRequest(1, 0);
            Application app = applicationService.apply(req);

            // 总价 = 5000, 订金 = 5000 × 1.00 = 5000（全款）
            assertEquals(0, app.getTotalAmount().compareTo(app.getDepositAmount()));
        }

        @Test
        @DisplayName("距出发<30天 → 订金=全款100%")
        void shouldCharge100PercentWhenLessThan30Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(10));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            ApplyRequest req = buildRequest(2, 0);
            Application app = applicationService.apply(req);

            // 总价 = 10000, 订金 = 10000 × 1.00 = 10000
            assertEquals(0, new BigDecimal("10000.00").compareTo(app.getDepositAmount()));
            assertEquals(app.getTotalAmount(), app.getDepositAmount());
        }

        @Test
        @DisplayName("仅小孩 — 订金按小孩价格计算")
        void shouldCalculateChildOnly() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(60));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            ApplyRequest req = buildRequest(0, 2);
            Application app = applicationService.apply(req);

            // 总价 = 3000×2 = 6000, 订金 = 600
            assertEquals(0, new BigDecimal("6000.00").compareTo(app.getTotalAmount()));
            assertEquals(0, new BigDecimal("600.00").compareTo(app.getDepositAmount()));
        }

        @Test
        @DisplayName("人数满额后状态变为FULL")
        void shouldSetGroupFullWhenCapacityReached() {
            testGroup.setMaxCapacity(10);
            testGroup.setCurrentCount(8);
            testGroup.setDepartureDate(LocalDate.now().plusDays(60));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            ApplyRequest req = buildRequest(2, 0); // 8+2=10, 刚好满
            applicationService.apply(req);

            assertEquals(10, testGroup.getCurrentCount());
            assertEquals("FULL", testGroup.getStatus());
        }
    }

    // ======================== 异常场景 ========================

    @Nested
    @DisplayName("申请异常场景")
    class ApplyErrorTests {

        @Test
        @DisplayName("异常 — 旅游团不存在")
        void shouldThrowWhenGroupNotFound() {
            when(tourGroupMapper.selectById("TG999")).thenReturn(null);

            ApplyRequest req = buildRequest(1, 0);
            req.setGroupCode("TG999");
            assertThrows(BusinessException.class, () -> applicationService.apply(req));
        }

        @Test
        @DisplayName("异常 — 已过截止日期")
        void shouldThrowWhenPastDeadline() {
            testGroup.setDeadline(LocalDate.now().minusDays(1));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);

            assertThrows(BusinessException.class, () -> applicationService.apply(buildRequest(1, 0)));
        }

        @Test
        @DisplayName("异常 — 人数已满")
        void shouldThrowWhenFull() {
            testGroup.setCurrentCount(30);
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);

            assertThrows(BusinessException.class, () -> applicationService.apply(buildRequest(1, 0)));
        }

        @Test
        @DisplayName("异常 — 价格未公开")
        void shouldThrowWhenPriceNotPublished() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(60));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(null); // 未公开

            assertThrows(BusinessException.class, () -> applicationService.apply(buildRequest(1, 0)));
        }
    }

    // ======================== 支付订金 ========================

    @Nested
    @DisplayName("支付订金")
    class PayDepositTests {

        @Test
        @DisplayName("正常支付订金 — 状态变为DEPOSIT_PAID")
        void shouldUpdateStatusAfterDeposit() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setStatus("DRAFT");
            app.setDepositAmount(new BigDecimal("2000.00"));
            when(applicationMapper.selectById(1L)).thenReturn(app);
            when(paymentMapper.insert(any())).thenReturn(1);
            when(applicationMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> applicationService.payDeposit(1L, 1001L));
            assertEquals("DEPOSIT_PAID", app.getStatus());
            assertEquals(0, new BigDecimal("2000.00").compareTo(app.getPaidAmount()));
        }

        @Test
        @DisplayName("异常 — 非DRAFT状态不可支付")
        void shouldThrowWhenNotDraft() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setStatus("DEPOSIT_PAID");
            when(applicationMapper.selectById(1L)).thenReturn(app);

            assertThrows(BusinessException.class, () -> applicationService.payDeposit(1L, 1001L));
        }
    }

    // ======================== 完成申请 ========================

    @Nested
    @DisplayName("完成申请")
    class CompleteApplicationTests {

        @Test
        @DisplayName("正常完成 — 状态变为COMPLETED")
        void shouldCompleteSuccessfully() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setStatus("DEPOSIT_PAID");
            when(applicationMapper.selectById(1L)).thenReturn(app);
            when(applicationMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> applicationService.completeApplication(1L));
            assertEquals("COMPLETED", app.getStatus());
            assertNotNull(app.getCompleteTime());
        }

        @Test
        @DisplayName("异常 — 申请不存在")
        void shouldThrowWhenNotFound() {
            when(applicationMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> applicationService.completeApplication(999L));
        }
    }

    // ======================== 边界值测试 ========================

    @Nested
    @DisplayName("订金比例 — 边界值详细测试")
    class DepositBoundaryTests {

        @Test
        @DisplayName("距出发59天 → 20% (边界：刚好低于60)")
        void boundaryAt59Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(59));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            Application app = applicationService.apply(buildRequest(1, 0));
            // 5000 × 20% = 1000
            assertEquals(0, new BigDecimal("1000.00").compareTo(app.getDepositAmount()));
        }

        @Test
        @DisplayName("距出发30天 → 20% (边界：刚好≥30)")
        void boundaryAt30Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(30));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            Application app = applicationService.apply(buildRequest(1, 0));
            assertEquals(0, new BigDecimal("1000.00").compareTo(app.getDepositAmount()));
        }

        @Test
        @DisplayName("距出发29天 → 100% (边界：刚好<30)")
        void boundaryAt29Days() {
            testGroup.setDepartureDate(LocalDate.now().plusDays(29));
            when(tourGroupMapper.selectById("TG001")).thenReturn(testGroup);
            when(tourPriceMapper.selectOne(any())).thenReturn(testPrice);
            when(applicationMapper.insert(any())).thenReturn(1);
            when(tourGroupMapper.updateById(any())).thenReturn(1);

            Application app = applicationService.apply(buildRequest(1, 0));
            assertEquals(app.getTotalAmount(), app.getDepositAmount());
        }
    }

    // ======================== 辅助方法 ========================

    private ApplyRequest buildRequest(int adults, int children) {
        ApplyRequest req = new ApplyRequest();
        req.setGroupCode("TG001");
        req.setContactName("张三");
        req.setContactPhone("13800138000");
        req.setAdultCount(adults);
        req.setChildCount(children);
        req.setEmployeeId(1001L);
        return req;
    }
}
