package com.manikagro.pms.service;

import com.manikagro.pms.domain.ActivityLog;
import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.domain.Project;
import com.manikagro.pms.dto.ApiDtos.ActivityPage;
import com.manikagro.pms.repository.ActivityLogRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {
    private final ActivityLogRepository logs;
    private final DtoMapper mapper;
    public ActivityService(ActivityLogRepository logs, DtoMapper mapper) { this.logs = logs; this.mapper = mapper; }

    public void record(Project project, Employee actor, String action, String description) {
        logs.save(new ActivityLog(project, actor, action, description));
    }

    @Transactional(readOnly = true)
    public ActivityPage recent(int page, int size, Long projectId) {
        int safePage = Math.max(0, page); int safeSize = Math.max(1, Math.min(size, 100));
        Page<ActivityLog> result = projectId == null
                ? logs.findAllByOrderByOccurredAtDesc(PageRequest.of(safePage, safeSize))
                : logs.findByProjectIdOrderByOccurredAtDesc(projectId, PageRequest.of(safePage, safeSize));
        return new ActivityPage(result.getContent().stream().map(mapper::activity).toList(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }
}
