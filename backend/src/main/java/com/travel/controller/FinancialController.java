package com.travel.controller;

import com.travel.dto.R;
import com.travel.service.FinancialExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialExportService exportService;

    /** 手动触发财务数据导出 */
    @PostMapping("/export")
    public R<Void> triggerExport() {
        exportService.dailyExport();
        return R.ok();
    }
}
