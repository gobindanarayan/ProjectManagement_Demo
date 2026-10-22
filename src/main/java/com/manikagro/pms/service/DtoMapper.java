package com.manikagro.pms.service;

import com.manikagro.pms.domain.*;
import com.manikagro.pms.dto.ApiDtos.*;
import com.manikagro.pms.repository.ExpenseRepository;
import com.manikagro.pms.repository.ProjectMemberRepository;
import com.manikagro.pms.repository.TaskRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    private final ExpenseRepository expenses;
    private final ProjectMemberRepository members;
    private final TaskRepository tasks;

    public DtoMapper(ExpenseRepository expenses, ProjectMemberRepository members, TaskRepository tasks) {
        this.expenses = expenses; this.members = members; this.tasks = tasks;
    }

    public EmployeeSummary employee(Employee employee) { return EmployeeSummary.from(employee); }

    public ProjectView project(Project project) {
        BigDecimal spent = FinancialCalculations.safe(expenses.totalByProjectId(project.getId()));
        BigDecimal budget = FinancialCalculations.safe(project.getApprovedBudget());
        BigDecimal remaining = FinancialCalculations.remainingBudget(budget, spent);
        double utilization = FinancialCalculations.utilizationPercent(budget, spent);
        long totalTasks = tasks.countByProjectId(project.getId());
        int progress = totalTasks == 0 ? 0 : (int) Math.round(tasks.countByProjectIdAndStatus(project.getId(), TaskStatus.COMPLETED) * 100.0 / totalTasks);
        return new ProjectView(project.getId(), project.getCode(), project.getName(), project.getDescription(), employee(project.getManager()),
                budget, spent, remaining, utilization, project.getStartDate(), project.getExpectedCompletionDate(), project.getStatus(),
                progress, project.isDemoData(), members.countByProjectIdAndActiveTrue(project.getId()), members.countByProjectId(project.getId()));
    }

    public ProjectMemberView member(ProjectMember member) {
        return new ProjectMemberView(member.getId(), employee(member.getEmployee()), member.getProjectRole(), member.getAssignedDate(), member.isActive());
    }
    public TaskView task(Task task) {
        return new TaskView(task.getId(), task.getTitle(), task.getDescription(), employee(task.getAssignee()), task.getPriority(),
                task.getStartDate(), task.getDueDate(), task.getStatus());
    }
    public ExpenseView expense(Expense expense) {
        return new ExpenseView(expense.getId(), expense.getTitle(), expense.getCategory(), expense.getAmount(), expense.getExpenseDate(),
                expense.getDescription(), employee(expense.getRecordedBy()));
    }
    public ActivityView activity(ActivityLog log) {
        Project project = log.getProject();
        return new ActivityView(log.getId(), project == null ? null : project.getId(), project == null ? "Project removed" : project.getName(),
                employee(log.getActor()), log.getAction(), log.getDescription(), log.getOccurredAt());
    }
    public ProjectDetail detail(Project project, List<ProjectMember> memberRows, List<Task> taskRows, List<Expense> expenseRows,
                                 List<CategoryTotal> breakdown) {
        return new ProjectDetail(project(project), memberRows.stream().map(this::member).toList(), taskRows.stream().map(this::task).toList(),
                expenseRows.stream().map(this::expense).toList(), breakdown);
    }
}
