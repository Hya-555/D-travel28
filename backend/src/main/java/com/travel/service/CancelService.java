package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.CancelRequest;
import com.travel.entity.ApplicationCancel;

public interface CancelService extends IService<ApplicationCancel> {

    /** 取消申请（整个或部分参加者） */
    ApplicationCancel cancel(CancelRequest req);
}
