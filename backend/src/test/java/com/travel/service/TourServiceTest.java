package com.travel.service;

import com.travel.config.BusinessException;
import com.travel.dto.GroupDTO;
import com.travel.dto.PriceDTO;
import com.travel.dto.RouteDTO;
import com.travel.entity.*;
import com.travel.mapper.*;
import com.travel.service.impl.TourServiceImpl;
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
 * 旅游产品管理服务单元测试
 * 核心测试: 路线CRUD+历史追踪、旅游团创建校验、价格公开锁定
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("旅游产品管理服务 - 单元测试")
class TourServiceTest {

    @Mock
    private TourRouteMapper routeMapper;
    @Mock
    private RouteHistoryMapper historyMapper;
    @Mock
    private TourGroupMapper groupMapper;
    @Mock
    private TourPriceMapper priceMapper;

    @InjectMocks
    private TourServiceImpl tourService;

    // ======================== 路线管理 ========================

    @Nested
    @DisplayName("路线创建")
    class RouteCreateTests {

        @Test
        @DisplayName("正常创建路线")
        void shouldCreateRoute() {
            when(routeMapper.selectById("RT001")).thenReturn(null);
            when(routeMapper.insert(any())).thenReturn(1);

            RouteDTO dto = new RouteDTO();
            dto.setRouteCode("RT001");
            dto.setRouteName("武汉-三亚");
            dto.setDescription("五日游");

            TourRoute result = tourService.createRoute(dto);

            assertEquals("RT001", result.getRouteCode());
            assertEquals("ACTIVE", result.getStatus());
        }

        @Test
        @DisplayName("异常 — 路线代码重复")
        void shouldThrowWhenDuplicate() {
            TourRoute existing = new TourRoute();
            existing.setRouteCode("RT001");
            when(routeMapper.selectById("RT001")).thenReturn(existing);

            RouteDTO dto = new RouteDTO();
            dto.setRouteCode("RT001");

            assertThrows(BusinessException.class, () -> tourService.createRoute(dto));
        }
    }

    @Nested
    @DisplayName("路线更新 — 历史追踪")
    class RouteUpdateTests {

        @Test
        @DisplayName("更新路线 — 生成变更历史")
        void shouldTrackHistoryOnUpdate() {
            TourRoute oldRoute = new TourRoute();
            oldRoute.setRouteCode("RT001");
            oldRoute.setRouteName("旧名称");
            oldRoute.setDescription("旧描述");
            oldRoute.setStatus("ACTIVE");
            when(routeMapper.selectById("RT001")).thenReturn(oldRoute);
            when(historyMapper.insert(any())).thenReturn(1);
            when(routeMapper.updateById(any())).thenReturn(1);

            RouteDTO dto = new RouteDTO();
            dto.setRouteName("新名称");
            dto.setDescription("新描述");
            dto.setEmployeeId(1001L);

            assertDoesNotThrow(() -> tourService.updateRoute("RT001", dto));
            assertEquals("新名称", oldRoute.getRouteName());
            verify(historyMapper).insert(any(RouteHistory.class));
        }

        @Test
        @DisplayName("异常 — 已取消的路线不可变更")
        void shouldThrowWhenInactive() {
            TourRoute oldRoute = new TourRoute();
            oldRoute.setRouteCode("RT001");
            oldRoute.setStatus("INACTIVE");
            when(routeMapper.selectById("RT001")).thenReturn(oldRoute);

            RouteDTO dto = new RouteDTO();
            dto.setRouteName("新名称");
            assertThrows(BusinessException.class, () -> tourService.updateRoute("RT001", dto));
        }
    }

    @Nested
    @DisplayName("路线取消")
    class RouteCancelTests {

        @Test
        @DisplayName("取消路线 — 状态变为INACTIVE + 记录历史")
        void shouldCancelAndTrackHistory() {
            TourRoute route = new TourRoute();
            route.setRouteCode("RT001");
            route.setStatus("ACTIVE");
            when(routeMapper.selectById("RT001")).thenReturn(route);
            when(routeMapper.updateById(any())).thenReturn(1);
            when(historyMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> tourService.cancelRoute("RT001"));
            assertEquals("INACTIVE", route.getStatus());
            verify(historyMapper).insert(any(RouteHistory.class));
        }
    }

    // ======================== 旅游团管理 ========================

    @Nested
    @DisplayName("旅游团创建")
    class GroupCreateTests {

        @Test
        @DisplayName("正常创建旅游团")
        void shouldCreateGroup() {
            TourRoute route = new TourRoute();
            route.setRouteCode("RT001");
            route.setStatus("ACTIVE");
            when(routeMapper.selectById("RT001")).thenReturn(route);
            when(groupMapper.insert(any())).thenReturn(1);

            GroupDTO dto = buildGroupDTO("TG001", 30);

            TourGroup result = tourService.createGroup(dto);

            assertEquals("TG001", result.getGroupCode());
            assertEquals(0, result.getCurrentCount());
            assertEquals("AVAILABLE", result.getStatus());
        }

        @Test
        @DisplayName("异常 — 路线不存在")
        void shouldThrowWhenRouteNotFound() {
            when(routeMapper.selectById("RT999")).thenReturn(null);

            GroupDTO dto = buildGroupDTO("TG001", 30);
            dto.setRouteCode("RT999");

            assertThrows(BusinessException.class, () -> tourService.createGroup(dto));
        }

        @Test
        @DisplayName("异常 — 出发日期早于截止日期")
        void shouldThrowWhenDepartureBeforeDeadline() {
            TourRoute route = new TourRoute();
            route.setRouteCode("RT001");
            when(routeMapper.selectById("RT001")).thenReturn(route);

            GroupDTO dto = new GroupDTO();
            dto.setGroupCode("TG001");
            dto.setRouteCode("RT001");
            dto.setDepartureDate(LocalDate.now().plusDays(10));
            dto.setDeadline(LocalDate.now().plusDays(20)); // 截止日期在出发之后
            dto.setMaxCapacity(30);

            assertThrows(BusinessException.class, () -> tourService.createGroup(dto));
        }
    }

    // ======================== 价格管理 ========================

    @Nested
    @DisplayName("价格设置 — 公开后锁定")
    class PriceSetTests {

        @Test
        @DisplayName("正常设置价格")
        void shouldSetPrice() {
            TourGroup group = new TourGroup();
            group.setGroupCode("TG001");
            when(groupMapper.selectById("TG001")).thenReturn(group);
            when(priceMapper.selectCount(any())).thenReturn(0L); // 无已公开价格
            when(priceMapper.insert(any())).thenReturn(1);

            PriceDTO dto = buildPriceDTO();

            TourPrice result = tourService.setPrice(dto);

            assertEquals(0, result.getIsPublished());
            assertEquals(0, new BigDecimal("5000.00").compareTo(result.getAdultPrice()));
            assertEquals(0, new BigDecimal("3000.00").compareTo(result.getChildPrice()));
        }

        @Test
        @DisplayName("异常 — 价格已公开后不可变更")
        void shouldThrowWhenAlreadyPublished() {
            TourGroup group = new TourGroup();
            group.setGroupCode("TG001");
            when(groupMapper.selectById("TG001")).thenReturn(group);
            when(priceMapper.selectCount(any())).thenReturn(1L); // 已有公开价格

            assertThrows(BusinessException.class, () -> tourService.setPrice(buildPriceDTO()));
        }

        @Test
        @DisplayName("异常 — 旅游团不存在")
        void shouldThrowWhenGroupNotFound() {
            when(groupMapper.selectById("TG999")).thenReturn(null);

            PriceDTO dto = buildPriceDTO();
            dto.setGroupCode("TG999");

            assertThrows(BusinessException.class, () -> tourService.setPrice(dto));
        }
    }

    @Nested
    @DisplayName("价格公开")
    class PricePublishTests {

        @Test
        @DisplayName("正常公开价格")
        void shouldPublishPrice() {
            TourPrice price = new TourPrice();
            price.setId(1L);
            price.setIsPublished(0);
            when(priceMapper.selectById(1L)).thenReturn(price);
            when(priceMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> tourService.publishPrice(1L));
            assertEquals(1, price.getIsPublished());
        }

        @Test
        @DisplayName("异常 — 重复公开")
        void shouldThrowWhenAlreadyPublished() {
            TourPrice price = new TourPrice();
            price.setId(1L);
            price.setIsPublished(1);
            when(priceMapper.selectById(1L)).thenReturn(price);

            assertThrows(BusinessException.class, () -> tourService.publishPrice(1L));
        }

        @Test
        @DisplayName("异常 — 价格记录不存在")
        void shouldThrowWhenPriceNotFound() {
            when(priceMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> tourService.publishPrice(999L));
        }
    }

    // ======================== 辅助方法 ========================

    private GroupDTO buildGroupDTO(String code, int capacity) {
        GroupDTO dto = new GroupDTO();
        dto.setGroupCode(code);
        dto.setRouteCode("RT001");
        dto.setDepartureDate(LocalDate.now().plusDays(60));
        dto.setDeadline(LocalDate.now().plusDays(30));
        dto.setMaxCapacity(capacity);
        return dto;
    }

    private PriceDTO buildPriceDTO() {
        PriceDTO dto = new PriceDTO();
        dto.setGroupCode("TG001");
        dto.setAdultPrice(new BigDecimal("5000.00"));
        dto.setChildPrice(new BigDecimal("3000.00"));
        dto.setDiscountDesc("早鸟优惠");
        dto.setEmployeeId(1001L);
        return dto;
    }
}
