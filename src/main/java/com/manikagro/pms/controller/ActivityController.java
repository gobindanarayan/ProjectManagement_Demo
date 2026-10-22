package com.manikagro.pms.controller;

import com.manikagro.pms.dto.ApiDtos.ActivityPage;
import com.manikagro.pms.service.ActivityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    private final ActivityService activities;
    public ActivityController(ActivityService activities) { this.activities = activities; }
    @GetMapping public ActivityPage recent(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(required = false) Long projectId) {
        return activities.recent(page, size, projectId);
    }
}
