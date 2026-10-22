package com.manikagro.pms.repository;

import com.manikagro.pms.domain.Task;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectIdOrderByDueDateAsc(Long projectId);
    List<Task> findByDueDateBetweenAndStatusNotOrderByDueDateAsc(LocalDate from, LocalDate to, com.manikagro.pms.domain.TaskStatus status);
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, com.manikagro.pms.domain.TaskStatus status);
}
