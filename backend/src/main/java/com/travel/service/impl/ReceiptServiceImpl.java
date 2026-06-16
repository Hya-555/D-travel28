package com.travel.service.impl;

import com.travel.config.BusinessException;
import com.travel.dto.PrintFormData;
import com.travel.entity.Application;
import com.travel.entity.Receipt;
import com.travel.entity.TourGroup;
import com.travel.entity.TourPrice;
import com.travel.entity.TourRoute;
import com.travel.mapper.ApplicationMapper;
import com.travel.mapper.ReceiptMapper;
import com.travel.mapper.TourGroupMapper;
import com.travel.mapper.TourPriceMapper;
import com.travel.mapper.TourRouteMapper;
import com.travel.service.ReceiptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptMapper receiptMapper;
    private final ApplicationMapper applicationMapper;
    private final TourGroupMapper tourGroupMapper;
    private final TourRouteMapper tourRouteMapper;
    private final TourPriceMapper tourPriceMapper;

    @Override
    @Transactional
    public Receipt printDepositReceipt(Long applicationId, Long employeeId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"DEPOSIT_PAID".equals(app.getStatus()) && !"COMPLETED".equals(app.getStatus())) {
            throw new BusinessException("当前状态不可打印收据");
        }

        Receipt receipt = new Receipt();
        receipt.setApplicationId(applicationId);
        receipt.setReceiptNo("RCP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        receipt.setReceiptType("DEPOSIT_RECEIPT");
        receipt.setPrintTime(LocalDateTime.now());
        receipt.setPrintedBy(employeeId);
        receiptMapper.insert(receipt);
        return receipt;
    }

    @Override
    @Transactional
    public List<Receipt> printDailyDocuments(Long employeeId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Application> completedApps = applicationMapper.findCompletedByDate(yesterday);

        List<Receipt> receipts = new ArrayList<>();
        for (Application app : completedApps) {
            boolean isFullPaid = app.getPaidAmount().compareTo(app.getTotalAmount()) >= 0;

            // 打印旅游确认书
            Receipt confirmation = new Receipt();
            confirmation.setApplicationId(app.getApplicationId());
            confirmation.setReceiptNo("CFM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            confirmation.setReceiptType("CONFIRMATION");
            confirmation.setPrintTime(LocalDateTime.now());
            confirmation.setPrintedBy(employeeId);
            receiptMapper.insert(confirmation);
            receipts.add(confirmation);

            // 未全款支付时，打印余额交款单
            if (!isFullPaid) {
                Receipt slip = new Receipt();
                slip.setApplicationId(app.getApplicationId());
                slip.setReceiptNo("SLP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                slip.setReceiptType("PAYMENT_SLIP");
                slip.setPrintTime(LocalDateTime.now());
                slip.setPrintedBy(employeeId);
                receiptMapper.insert(slip);
                receipts.add(slip);
            }
        }
        return receipts;
    }

    @Override
    public PrintFormData getPrintFormData(Long applicationId) {
        // 1. 查询申请
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }

        // 2. 查询旅游团
        TourGroup group = tourGroupMapper.selectById(app.getGroupCode());
        if (group == null) {
            throw new BusinessException("旅游团不存在");
        }

        // 3. 查询路线
        TourRoute route = tourRouteMapper.selectById(group.getRouteCode());
        if (route == null) {
            throw new BusinessException("路线不存在");
        }

        // 4. 查询最新公开价格（可能为空——价格被取消公开时回退为零）
        TourPrice price = tourPriceMapper.selectOne(new LambdaQueryWrapper<TourPrice>()
                .eq(TourPrice::getGroupCode, app.getGroupCode())
                .eq(TourPrice::getIsPublished, 1)
                .orderByDesc(TourPrice::getSetTime)
                .last("LIMIT 1"));

        BigDecimal adultPrice = price != null ? price.getAdultPrice() : BigDecimal.ZERO;
        BigDecimal childPrice = price != null ? price.getChildPrice() : BigDecimal.ZERO;
        String discountDesc = price != null ? price.getDiscountDesc() : "";

        // 5. 组装 PrintFormData
        PrintFormData data = new PrintFormData();
        // 申请信息
        data.setApplicationId(app.getApplicationId());
        data.setGroupCode(app.getGroupCode());
        data.setDepartureDate(app.getDepartureDate());
        data.setContactName(app.getContactName());
        data.setContactPhone(app.getContactPhone());
        data.setAdultCount(app.getAdultCount());
        data.setChildCount(app.getChildCount());
        data.setDepositAmount(app.getDepositAmount());
        data.setTotalAmount(app.getTotalAmount());
        data.setPaidAmount(app.getPaidAmount());
        data.setStatus(app.getStatus());
        data.setApplyTime(app.getApplyTime());
        // 旅游团信息
        data.setRouteCode(group.getRouteCode());
        data.setDeadline(group.getDeadline());
        // 路线信息
        data.setRouteName(route.getRouteName());
        data.setRouteDescription(route.getDescription());
        // 价格信息
        data.setAdultPrice(adultPrice);
        data.setChildPrice(childPrice);
        data.setDiscountDesc(discountDesc);
        // 计算字段
        data.setAdultSubtotal(adultPrice.multiply(BigDecimal.valueOf(app.getAdultCount())));
        data.setChildSubtotal(childPrice.multiply(BigDecimal.valueOf(app.getChildCount())));
        data.setBalanceDue(app.getTotalAmount().subtract(app.getPaidAmount()));

        return data;
    }
}
