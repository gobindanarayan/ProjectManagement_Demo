package com.manikagro.pms.controller;

import com.manikagro.pms.dto.ApiDtos.DashboardData;
import com.manikagro.pms.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }
    @GetMapping public DashboardData get() { return dashboard.get(); }
}
