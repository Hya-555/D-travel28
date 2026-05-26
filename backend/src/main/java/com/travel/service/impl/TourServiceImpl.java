package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.config.BusinessException;
import com.travel.dto.GroupDTO;
import com.travel.dto.PriceDTO;
import com.travel.dto.RouteDTO;
import com.travel.entity.*;
import com.travel.mapper.*;
import com.travel.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRouteMapper routeMapper;
    private final RouteHistoryMapper historyMapper;
    private final TourGroupMapper groupMapper;
    private final TourPriceMapper priceMapper;

    @Override
    @Transactional
    public TourRoute createRoute(RouteDTO dto) {
        if (routeMapper.selectById(dto.getRouteCode()) != null) {
            throw new BusinessException("路线代码已存在");
        }
        TourRoute route = new TourRoute();
        route.setRouteCode(dto.getRouteCode());
        route.setRouteName(dto.getRouteName());
        route.setDescription(dto.getDescription());
        route.setStatus("ACTIVE");
        routeMapper.insert(route);
        return route;
    }

    @Override
    @Transactional
    public TourGroup createGroup(GroupDTO dto) {
        TourRoute route = routeMapper.selectById(dto.getRouteCode());
        if (route == null) {
            throw new BusinessException("旅游路线不存在");
        }
        if (dto.getDepartureDate().isBefore(dto.getDeadline())) {
            throw new BusinessException("出发日期不能早于截止日期");
        }
        TourGroup group = new TourGroup();
        group.setGroupCode(dto.getGroupCode());
        group.setRouteCode(dto.getRouteCode());
        group.setDepartureDate(dto.getDepartureDate());
        group.setDeadline(dto.getDeadline());
        group.setMaxCapacity(dto.getMaxCapacity());
        group.setCurrentCount(0);
        group.setStatus("AVAILABLE");
        groupMapper.insert(group);
        return group;
    }

    @Override
    @Transactional
    public TourPrice setPrice(PriceDTO dto) {
        TourGroup group = groupMapper.selectById(dto.getGroupCode());
        if (group == null) {
            throw new BusinessException("旅游团不存在");
        }
        // 检查是否已有公开的价格，公开后不可变更
        Long publishedCount = priceMapper.selectCount(new LambdaQueryWrapper<TourPrice>()
                .eq(TourPrice::getGroupCode, dto.getGroupCode())
                .eq(TourPrice::getIsPublished, 1));
        if (publishedCount > 0) {
            throw new BusinessException("该旅游团价格已公开，不可变更");
        }

        TourPrice price = new TourPrice();
        price.setGroupCode(dto.getGroupCode());
        price.setAdultPrice(dto.getAdultPrice());
        price.setChildPrice(dto.getChildPrice());
        price.setDiscountDesc(dto.getDiscountDesc());
        price.setIsPublished(0);
        price.setSetTime(LocalDateTime.now());
        price.setSetBy(dto.getEmployeeId());
        priceMapper.insert(price);
        return price;
    }

    @Override
    @Transactional
    public void publishPrice(Long priceId) {
        TourPrice price = priceMapper.selectById(priceId);
        if (price == null) {
            throw new BusinessException("价格记录不存在");
        }
        if (price.getIsPublished() == 1) {
            throw new BusinessException("该价格已公开");
        }
        price.setIsPublished(1);
        priceMapper.updateById(price);
    }

    @Override
    @Transactional
    public TourRoute updateRoute(String routeCode, RouteDTO dto) {
        TourRoute oldRoute = routeMapper.selectById(routeCode);
        if (oldRoute == null) {
            throw new BusinessException("路线不存在");
        }
        if ("INACTIVE".equals(oldRoute.getStatus())) {
            throw new BusinessException("已取消的路线不可变更");
        }

        // 记录变更历史
        RouteHistory history = new RouteHistory();
        history.setRouteCode(routeCode);
        history.setChangeType("UPDATE");
        history.setOldValue("{\"routeName\":\"" + oldRoute.getRouteName() + "\",\"description\":\"" + oldRoute.getDescription() + "\"}");
        history.setNewValue("{\"routeName\":\"" + dto.getRouteName() + "\",\"description\":\"" + dto.getDescription() + "\"}");
        history.setChangeTime(LocalDateTime.now());
        history.setOperatorId(dto.getEmployeeId());
        historyMapper.insert(history);

        // 更新路线
        oldRoute.setRouteName(dto.getRouteName());
        oldRoute.setDescription(dto.getDescription());
        routeMapper.updateById(oldRoute);
        return oldRoute;
    }

    @Override
    @Transactional
    public void cancelRoute(String routeCode) {
        TourRoute route = routeMapper.selectById(routeCode);
        if (route == null) {
            throw new BusinessException("路线不存在");
        }
        route.setStatus("INACTIVE");
        routeMapper.updateById(route);

        RouteHistory history = new RouteHistory();
        history.setRouteCode(routeCode);
        history.setChangeType("CANCEL");
        history.setOldValue("{\"status\":\"ACTIVE\"}");
        history.setNewValue("{\"status\":\"INACTIVE\"}");
        history.setChangeTime(LocalDateTime.now());
        historyMapper.insert(history);
    }
}
