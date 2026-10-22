package com.manikagro.pms.dto;

import com.manikagro.pms.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ApiDtos {
    private ApiDtos() {}

    public record ProjectRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9_-]{1,31}", message = "Use 2–32 letters, numbers, hyphens, or underscores") String code,
            @NotBlank @Size(max = 1200) String description,
            @NotNull Long managerId,
            @NotNull @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal approvedBudget,
            @NotNull LocalDate startDate,
            @NotNull LocalDate expectedCompletionDate,
            @NotNull ProjectStatus status,
            boolean demoData) {}

    public record ExpenseRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 80) String category,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 13, fraction = 2) BigDecimal amount,
            @NotNull LocalDate expenseDate,
            @NotBlank @Size(max = 1200) String description) {}

    public record TaskRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 1200) String description,
            Long assigneeId,
            @NotNull TaskPriority priority,
            @NotNull LocalDate startDate,
            @NotNull LocalDate dueDate,
            @NotNull TaskStatus status) {}

    public record MemberRequest(@NotNull Long employeeId, @NotBlank @Size(max = 100) String projectRole,
                                @NotNull LocalDate assignedDate, boolean active) {}

    public record EmployeeRequest(
            @NotBlank @Size(max = 32) String employeeCode,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 80) String username,
            @NotBlank @Email @Size(max = 190) String email,
            @Size(max = 100) @Pattern(regexp = "^$|.{12,100}", message = "Password must be blank or at least 12 characters") String password,
            @NotBlank @Size(max = 100) String jobTitle,
            @NotNull EmployeeRole role,
            boolean active) {}

    public record EmployeeView(Long id, String employeeCode, String name, String username, String email,
                               String jobTitle, EmployeeRole role, boolean active) {
        public static EmployeeView from(Employee e) {
            return new EmployeeView(e.getId(), e.getEmployeeCode(), e.getName(), e.getUsername(), e.getEmail(), e.getJobTitle(), e.getRole(), e.isActive());
        }
    }

    public record EmployeeSummary(Long id, String employeeCode, String name, String jobTitle, EmployeeRole role, boolean active) {
        public static EmployeeSummary from(Employee e) {
            return e == null ? null : new EmployeeSummary(e.getId(), e.getEmployeeCode(), e.getName(), e.getJobTitle(), e.getRole(), e.isActive());
        }
    }

    public record ProjectView(Long id, String code, String name, String description, EmployeeSummary manager,
                              BigDecimal approvedBudget, BigDecimal expenditure, BigDecimal remainingBudget,
                              double budgetUtilization, LocalDate startDate, LocalDate expectedCompletionDate,
                              ProjectStatus status, int progress, boolean demoData, long activeMembers, long assignedMembers) {}

    public record ProjectMemberView(Long id, EmployeeSummary employee, String projectRole, LocalDate assignedDate, boolean active) {}
    public record TaskView(Long id, String title, String description, EmployeeSummary assignee, TaskPriority priority,
                           LocalDate startDate, LocalDate dueDate, TaskStatus status) {}
    public record ExpenseView(Long id, String title, String category, BigDecimal amount, LocalDate expenseDate,
                              String description, EmployeeSummary recordedBy) {}
    public record ActivityView(Long id, Long projectId, String projectName, EmployeeSummary actor, String action,
                               String description, LocalDateTime occurredAt) {}
    public record ActivityPage(List<ActivityView> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {}
    public record UpcomingTaskView(Long id, String title, String projectName, LocalDate dueDate, TaskPriority priority,
                                   TaskStatus status, EmployeeSummary assignee) {}
    public record CategoryTotal(String category, BigDecimal amount) {}
    public record ProjectDetail(ProjectView project, List<ProjectMemberView> members, List<TaskView> tasks,
                                List<ExpenseView> expenses, List<CategoryTotal> expenseBreakdown) {}
    public record DashboardStats(long totalProjects, long activeProjects, BigDecimal totalBudget,
                                 BigDecimal totalExpenditure, BigDecimal remainingBudget, long activeTeamMembers,
                                 long upcomingTasks) {}
    public record DashboardData(DashboardStats stats, List<ProjectView> projects,
                                List<ActivityView> recentActivities, List<UpcomingTaskView> upcomingTasks) {}
    public record ErrorResponse(String message, Map<String, String> details, LocalDateTime timestamp) {}
    public record MessageResponse(String message) {}
}
