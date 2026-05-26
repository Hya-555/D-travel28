package com.travel.service;

import com.travel.dto.*;
import com.travel.entity.*;

public interface TourService {

    /** 录入新旅游路线 */
    TourRoute createRoute(RouteDTO dto);

    /** 创建旅游团 */
    TourGroup createGroup(GroupDTO dto);

    /** 设定旅游团价格（未公开时可多次设定） */
    TourPrice setPrice(PriceDTO dto);

    /** 公开价格（公开后不可变更） */
    void publishPrice(Long priceId);

    /** 变更路线（保留历史） */
    TourRoute updateRoute(String routeCode, RouteDTO dto);

    /** 取消路线（不删除，状态变更） */
    void cancelRoute(String routeCode);
}
