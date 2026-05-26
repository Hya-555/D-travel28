package com.travel.controller;

import com.travel.dto.*;
import com.travel.entity.*;
import com.travel.mapper.TourGroupMapper;
import com.travel.mapper.TourPriceMapper;
import com.travel.mapper.TourRouteMapper;
import com.travel.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final TourRouteMapper routeMapper;
    private final TourGroupMapper groupMapper;
    private final TourPriceMapper priceMapper;

    // ====== 路线管理 ======

    /** 录入新路线 */
    @PostMapping("/route")
    public R<TourRoute> createRoute(@RequestBody RouteDTO dto) {
        return R.ok(tourService.createRoute(dto));
    }

    /** 变更路线 */
    @PutMapping("/route/{routeCode}")
    public R<TourRoute> updateRoute(@PathVariable String routeCode, @RequestBody RouteDTO dto) {
        return R.ok(tourService.updateRoute(routeCode, dto));
    }

    /** 取消路线 */
    @PostMapping("/route/{routeCode}/cancel")
    public R<Void> cancelRoute(@PathVariable String routeCode) {
        tourService.cancelRoute(routeCode);
        return R.ok();
    }

    /** 查询所有路线 */
    @GetMapping("/routes")
    public R<List<TourRoute>> listRoutes() {
        return R.ok(routeMapper.selectList(null));
    }

    // ====== 旅游团管理 ======

    /** 创建旅游团 */
    @PostMapping("/group")
    public R<TourGroup> createGroup(@RequestBody GroupDTO dto) {
        return R.ok(tourService.createGroup(dto));
    }

    /** 查询所有旅游团 */
    @GetMapping("/groups")
    public R<List<TourGroup>> listGroups() {
        return R.ok(groupMapper.selectList(null));
    }

    /** 查询可报名的旅游团 */
    @GetMapping("/groups/available")
    public R<List<TourGroup>> listAvailableGroups() {
        List<TourGroup> list = groupMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TourGroup>()
                        .eq(TourGroup::getStatus, "AVAILABLE"));
        return R.ok(list);
    }

    // ====== 价格管理 ======

    /** 设定价格 */
    @PostMapping("/price")
    public R<TourPrice> setPrice(@RequestBody PriceDTO dto) {
        return R.ok(tourService.setPrice(dto));
    }

    /** 公开价格 */
    @PostMapping("/price/{priceId}/publish")
    public R<Void> publishPrice(@PathVariable Long priceId) {
        tourService.publishPrice(priceId);
        return R.ok();
    }

    /** 查询旅游团的价格历史 */
    @GetMapping("/prices/{groupCode}")
    public R<List<TourPrice>> listPrices(@PathVariable String groupCode) {
        List<TourPrice> list = priceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TourPrice>()
                        .eq(TourPrice::getGroupCode, groupCode)
                        .orderByDesc(TourPrice::getSetTime));
        return R.ok(list);
    }
}
