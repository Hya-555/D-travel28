package com.travel.controller;

import com.travel.dto.CancelRequest;
import com.travel.dto.R;
import com.travel.entity.ApplicationCancel;
import com.travel.service.CancelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cancel")
@RequiredArgsConstructor
public class CancelController {

    private final CancelService cancelService;

    /** 取消申请或变更参加者 */
    @PostMapping
    public R<ApplicationCancel> cancel(@RequestBody CancelRequest req) {
        return R.ok(cancelService.cancel(req));
    }
}
