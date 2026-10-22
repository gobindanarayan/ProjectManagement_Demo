package com.manikagro.pms.service;

import com.manikagro.pms.domain.Project;
import com.manikagro.pms.domain.ProjectStatus;
import com.manikagro.pms.domain.Task;
import com.manikagro.pms.domain.TaskStatus;
import com.manikagro.pms.dto.ApiDtos.*;
import com.manikagro.pms.repository.ExpenseRepository;
import com.manikagro.pms.repository.ProjectMemberRepository;
import com.manikagro.pms.repository.ProjectRepository;
import com.manikagro.pms.repository.TaskRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ProjectRepository projects;
    private final ExpenseRepository expenses;
    private final ProjectMemberRepository members;
    private final TaskRepository tasks;
    private final ActivityService activities;
    private final DtoMapper mapper;
    public DashboardService(ProjectRepository projects, ExpenseRepository expenses, ProjectMemberRepository members,
                            TaskRepository tasks, ActivityService activities, DtoMapper mapper) {
        this.projects = projects; this.expenses = expenses; this.members = members; this.tasks = tasks; this.activities = activities; this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DashboardData get() {
        List<Project> rows = projects.findAllByOrderByUpdatedAtDesc();
        BigDecimal totalBudget = rows.stream().map(Project::getApprovedBudget).map(FinancialCalculations::safe).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spent = FinancialCalculations.safe(expenses.totalAll());
        LocalDate today = LocalDate.now();
        List<Task> upcoming = tasks.findByDueDateBetweenAndStatusNotOrderByDueDateAsc(today, today.plusDays(14), TaskStatus.COMPLETED);
        List<UpcomingTaskView> upcomingViews = upcoming.stream().limit(8)
                .map(task -> new UpcomingTaskView(task.getId(), task.getTitle(), task.getProject().getName(), task.getDueDate(), task.getPriority(), task.getStatus(), mapper.employee(task.getAssignee()))).toList();
        DashboardStats stats = new DashboardStats(rows.size(), rows.stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count(),
                totalBudget, spent, FinancialCalculations.remainingBudget(totalBudget, spent), members.countDistinctActiveEmployees(), upcoming.size());
        return new DashboardData(stats, rows.stream().map(mapper::project).toList(), activities.recent(0, 6, null).content(), upcomingViews);
    }
}
