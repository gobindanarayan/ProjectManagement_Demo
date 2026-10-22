package com.manikagro.pms.repository;

import com.manikagro.pms.domain.ActivityLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
    Page<ActivityLog> findByProjectIdOrderByOccurredAtDesc(Long projectId, Pageable pageable);
    List<ActivityLog> findByProjectId(Long projectId);
}
