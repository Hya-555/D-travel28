package com.travel.service;

import com.travel.config.BusinessException;
import com.travel.entity.Application;
import com.travel.entity.Receipt;
import com.travel.mapper.ApplicationMapper;
import com.travel.mapper.ReceiptMapper;
import com.travel.service.impl.ReceiptServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 收据打印服务单元测试
 * 核心测试: 订金收据生成、每日批量打印（确认书+余额交款单）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("收据打印服务 - 单元测试")
class ReceiptServiceTest {

    @Mock
    private ReceiptMapper receiptMapper;
    @Mock
    private ApplicationMapper applicationMapper;

    @InjectMocks
    private ReceiptServiceImpl receiptService;

    // ======================== 单张订金收据 ========================

    @Nested
    @DisplayName("单张订金收据打印")
    class DepositReceiptTests {

        @Test
        @DisplayName("正常打印订金收据 — 状态DEPOSIT_PAID")
        void shouldPrintDepositReceipt() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setStatus("DEPOSIT_PAID");
            when(applicationMapper.selectById(1L)).thenReturn(app);
            when(receiptMapper.insert(any())).thenReturn(1);

            Receipt receipt = receiptService.printDepositReceipt(1L, 1001L);

            assertNotNull(receipt);
            assertEquals(1L, receipt.getApplicationId());
            assertEquals("DEPOSIT_RECEIPT", receipt.getReceiptType());
            assertEquals(1001L, receipt.getPrintedBy());
            assertNotNull(receipt.getReceiptNo());
            assertTrue(receipt.getReceiptNo().startsWith("RCP"));
        }

        @Test
        @DisplayName("正常打印 — 状态COMPLETED也可以打印")
        void shouldPrintWhenCompleted() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setStatus("COMPLETED");
            when(applicationMapper.selectById(1L)).thenReturn(app);
            when(receiptMapper.insert(any())).thenReturn(1);

            Receipt receipt = receiptService.printDepositReceipt(1L, 1001L);

            assertNotNull(receipt);
        }

        @Test
        @DisplayName("异常 — DRAFT状态不可打印")
        void shouldThrowWhenDraft() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setStatus("DRAFT");
            when(applicationMapper.selectById(1L)).thenReturn(app);

            assertThrows(BusinessException.class,
                    () -> receiptService.printDepositReceipt(1L, 1001L));
        }

        @Test
        @DisplayName("异常 — 申请不存在")
        void shouldThrowWhenNotFound() {
            when(applicationMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> receiptService.printDepositReceipt(999L, 1001L));
        }
    }

    // ======================== 每日批量打印 ========================

    @Nested
    @DisplayName("每日批量打印")
    class DailyBatchTests {

        @Test
        @DisplayName("无昨日完成申请 — 返回空列表")
        void shouldReturnEmptyWhenNoCompleted() {
            when(applicationMapper.findCompletedByDate(any())).thenReturn(Collections.emptyList());

            List<Receipt> result = receiptService.printDailyDocuments(1001L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("已全款支付 — 仅打印确认书")
        void shouldPrintOnlyConfirmationWhenFullyPaid() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setPaidAmount(new BigDecimal("10000.00"));
            app.setTotalAmount(new BigDecimal("10000.00"));
            when(applicationMapper.findCompletedByDate(any())).thenReturn(List.of(app));
            when(receiptMapper.insert(any())).thenReturn(1);

            List<Receipt> result = receiptService.printDailyDocuments(1001L);

            assertEquals(1, result.size());
            assertEquals("CONFIRMATION", result.get(0).getReceiptType());
            assertTrue(result.get(0).getReceiptNo().startsWith("CFM"));
        }

        @Test
        @DisplayName("未全款支付 — 打印确认书 + 余额交款单")
        void shouldPrintBothWhenNotFullyPaid() {
            Application app = new Application();
            app.setApplicationId(1L);
            app.setPaidAmount(new BigDecimal("2000.00"));
            app.setTotalAmount(new BigDecimal("10000.00"));
            when(applicationMapper.findCompletedByDate(any())).thenReturn(List.of(app));
            when(receiptMapper.insert(any())).thenReturn(1);

            List<Receipt> result = receiptService.printDailyDocuments(1001L);

            assertEquals(2, result.size());
            assertEquals("CONFIRMATION", result.get(0).getReceiptType());
            assertEquals("PAYMENT_SLIP", result.get(1).getReceiptType());
            assertTrue(result.get(1).getReceiptNo().startsWith("SLP"));
        }

        @Test
        @DisplayName("多申请批量打印")
        void shouldBatchMultipleApplications() {
            Application app1 = new Application();
            app1.setApplicationId(1L);
            app1.setPaidAmount(new BigDecimal("10000.00"));
            app1.setTotalAmount(new BigDecimal("10000.00"));

            Application app2 = new Application();
            app2.setApplicationId(2L);
            app2.setPaidAmount(new BigDecimal("2000.00"));
            app2.setTotalAmount(new BigDecimal("8000.00"));

            when(applicationMapper.findCompletedByDate(any())).thenReturn(List.of(app1, app2));
            when(receiptMapper.insert(any())).thenReturn(1);

            List<Receipt> result = receiptService.printDailyDocuments(1001L);

            // app1→仅确认书(1), app2→确认书(1)+交款单(1)=3
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("打印时间应为昨天")
        void shouldQueryYesterday() {
            when(applicationMapper.findCompletedByDate(any())).thenReturn(Collections.emptyList());

            receiptService.printDailyDocuments(1001L);

            verify(applicationMapper).findCompletedByDate(LocalDate.now().minusDays(1));
        }
    }
}
