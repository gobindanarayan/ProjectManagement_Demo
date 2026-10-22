package com.manikagro.pms.service;

import com.manikagro.pms.domain.*;
import com.manikagro.pms.repository.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HexFormat;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "pms.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements ApplicationRunner {
    private final EmployeeRepository employees;
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final TaskRepository tasks;
    private final ExpenseRepository expenses;
    private final ActivityLogRepository activity;
    private final PasswordEncoder passwords;
    private final SecureRandom random = new SecureRandom();

    public DemoDataInitializer(EmployeeRepository employees, ProjectRepository projects, ProjectMemberRepository members,
                               TaskRepository tasks, ExpenseRepository expenses, ActivityLogRepository activity,
                               PasswordEncoder passwords) {
        this.employees = employees; this.projects = projects; this.members = members; this.tasks = tasks;
        this.expenses = expenses; this.activity = activity; this.passwords = passwords;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (projects.count() > 0) return;
        Employee manager = employee("DEMO-EMP-01", "Demo Manager One", "demo.manager1", "manager1@example.invalid", "Project Manager");
        Employee coordinator = employee("DEMO-EMP-02", "Demo Coordinator Two", "demo.coordinator2", "coordinator2@example.invalid", "Project Coordinator");
        Employee analyst = employee("DEMO-EMP-03", "Demo Analyst Three", "demo.analyst3", "analyst3@example.invalid", "Finance Analyst");
        LocalDate today = LocalDate.now();

        Project ajah = project("AJAH-001", "AJAH",
                "Livelihood and franchise-support initiative. Project details and operational scope must be verified with authorized company personnel.",
                manager, "850000.00", today.minusDays(62), today.plusDays(143), ProjectStatus.ACTIVE);
        Project rural = project("DEMO-002", "Demo Project – Rural Development",
                "FICTIONAL DEMO PROJECT. Sample rural development tracking data for demonstrating budgets, tasks, and team coordination.",
                coordinator, "560000.00", today.minusDays(35), today.plusDays(110), ProjectStatus.ACTIVE);
        Project supply = project("DEMO-003", "Demo Project – Supply Chain Operations",
                "FICTIONAL DEMO PROJECT. Sample supply chain tracking data for demonstrating expenditure and operational milestones.",
                analyst, "720000.00", today.minusDays(18), today.plusDays(155), ProjectStatus.PLANNING);

        addMember(ajah, manager, "Project Manager", today.minusDays(62)); addMember(ajah, coordinator, "Field Coordinator", today.minusDays(55)); addMember(ajah, analyst, "Finance Support", today.minusDays(48));
        addMember(rural, coordinator, "Project Manager", today.minusDays(35)); addMember(rural, manager, "Program Advisor", today.minusDays(30)); addMember(rural, analyst, "Finance Support", today.minusDays(28));
        addMember(supply, analyst, "Project Manager", today.minusDays(18)); addMember(supply, manager, "Operations Lead", today.minusDays(14)); addMember(supply, coordinator, "Logistics Coordinator", today.minusDays(12));

        addExpense(ajah, manager, "Community consultation materials", "Operations", "48250.00", today.minusDays(32));
        addExpense(ajah, analyst, "Field travel and logistics", "Travel", "127600.00", today.minusDays(17));
        addExpense(ajah, coordinator, "Training venue and supplies", "Training", "67000.00", today.minusDays(5));
        addExpense(rural, coordinator, "Village workshop supplies", "Training", "68400.00", today.minusDays(20));
        addExpense(rural, analyst, "Baseline survey travel", "Travel", "117800.00", today.minusDays(8));
        addExpense(supply, analyst, "Warehouse assessment", "Operations", "193400.00", today.minusDays(9));
        addExpense(supply, manager, "Route planning tools", "Equipment", "125000.00", today.minusDays(3));

        addTask(ajah, "Confirm operational scope", "Fictional sample task. Verify initiative details with authorized company personnel.", manager, TaskPriority.HIGH, today.minusDays(42), today.minusDays(20), TaskStatus.COMPLETED);
        addTask(ajah, "Prepare field engagement plan", "Fictional sample task for planning field coordination activities.", coordinator, TaskPriority.MEDIUM, today.minusDays(10), today.plusDays(3), TaskStatus.IN_PROGRESS);
        addTask(ajah, "Review budget assumptions", "Fictional sample task for finance review.", analyst, TaskPriority.HIGH, today.minusDays(8), today.plusDays(6), TaskStatus.TO_DO);
        addTask(ajah, "Draft implementation checklist", "Fictional sample task for implementation planning.", coordinator, TaskPriority.MEDIUM, today.minusDays(2), today.plusDays(12), TaskStatus.TO_DO);
        addTask(ajah, "Stakeholder follow-up", "Fictional sample task awaiting a response.", manager, TaskPriority.LOW, today.minusDays(4), today.plusDays(9), TaskStatus.BLOCKED);
        addTask(rural, "Map demonstration locations", "Fictional sample task for demo project.", coordinator, TaskPriority.MEDIUM, today.minusDays(25), today.minusDays(10), TaskStatus.COMPLETED);
        addTask(rural, "Schedule community workshops", "Fictional sample task for demo project.", manager, TaskPriority.HIGH, today.minusDays(4), today.plusDays(2), TaskStatus.IN_PROGRESS);
        addTask(rural, "Prepare participant materials", "Fictional sample task for demo project.", analyst, TaskPriority.LOW, today, today.plusDays(11), TaskStatus.TO_DO);
        addTask(rural, "Summarize baseline findings", "Fictional sample task for demo project.", analyst, TaskPriority.MEDIUM, today.plusDays(2), today.plusDays(15), TaskStatus.TO_DO);
        addTask(supply, "Assess warehouse capacity", "Fictional sample task for demo project.", analyst, TaskPriority.HIGH, today.minusDays(13), today.minusDays(2), TaskStatus.COMPLETED);
        addTask(supply, "Review route assumptions", "Fictional sample task for demo project.", manager, TaskPriority.MEDIUM, today.minusDays(5), today.plusDays(4), TaskStatus.IN_PROGRESS);
        addTask(supply, "Document inventory handoffs", "Fictional sample task for demo project.", coordinator, TaskPriority.MEDIUM, today.minusDays(1), today.plusDays(10), TaskStatus.TO_DO);
        addTask(supply, "Prepare supplier checklist", "Fictional sample task for demo project.", coordinator, TaskPriority.LOW, today.plusDays(1), today.plusDays(16), TaskStatus.TO_DO);

        activity.save(new ActivityLog(ajah, manager, "PROJECT_CREATED", "Initialized AJAH with clearly identified fictional demo values"));
        activity.save(new ActivityLog(rural, coordinator, "PROJECT_CREATED", "Initialized fictional demo project"));
        activity.save(new ActivityLog(supply, analyst, "PROJECT_CREATED", "Initialized fictional demo project"));
    }

    private Employee employee(String code, String name, String username, String email, String title) {
        return employees.findByEmployeeCodeIgnoreCase(code).orElseGet(() -> employees.save(new Employee(code, name, username, email,
                passwords.encode(randomSecret()), title, EmployeeRole.EMPLOYEE, true)));
    }
    private String randomSecret() { byte[] bytes = new byte[32]; random.nextBytes(bytes); return HexFormat.of().formatHex(bytes); }
    private Project project(String code, String name, String description, Employee manager, String budget, LocalDate start, LocalDate due, ProjectStatus status) {
        return projects.save(new Project(code, name, description, manager, new BigDecimal(budget), start, due, status, true));
    }
    private void addMember(Project p, Employee e, String role, LocalDate date) { members.save(new ProjectMember(p, e, role, date, true)); }
    private void addExpense(Project p, Employee by, String title, String category, String amount, LocalDate date) {
        expenses.save(new Expense(p, title, category, new BigDecimal(amount), date, "Fictional demo expense for sample data.", by));
    }
    private void addTask(Project p, String title, String description, Employee assignee, TaskPriority priority, LocalDate start, LocalDate due, TaskStatus status) {
        tasks.save(new Task(p, title, description, assignee, priority, start, due, status));
    }
}
