package com.manikagro.pms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_project_status", columnList = "status"),
        @Index(name = "idx_project_start_date", columnList = "start_date")
}, uniqueConstraints = @UniqueConstraint(name = "uk_project_code", columnNames = "project_code"))
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "project_code", nullable = false, length = 32)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, length = 1200)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_manager"))
    private Employee manager;
    @Column(name = "approved_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal approvedBudget;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "expected_completion_date", nullable = false)
    private LocalDate expectedCompletionDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ProjectStatus status;
    @Column(name = "demo_data", nullable = false)
    private boolean demoData;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected Project() {}

    public Project(String code, String name, String description, Employee manager, BigDecimal approvedBudget,
                   LocalDate startDate, LocalDate expectedCompletionDate, ProjectStatus status, boolean demoData) {
        this.code = code; this.name = name; this.description = description; this.manager = manager;
        this.approvedBudget = approvedBudget; this.startDate = startDate; this.expectedCompletionDate = expectedCompletionDate;
        this.status = status; this.demoData = demoData;
    }

    @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }
    public BigDecimal getApprovedBudget() { return approvedBudget; }
    public void setApprovedBudget(BigDecimal approvedBudget) { this.approvedBudget = approvedBudget; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getExpectedCompletionDate() { return expectedCompletionDate; }
    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) { this.expectedCompletionDate = expectedCompletionDate; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public boolean isDemoData() { return demoData; }
    public void setDemoData(boolean demoData) { this.demoData = demoData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
