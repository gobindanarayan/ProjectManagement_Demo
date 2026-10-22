package com.manikagro.pms.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_tasks", indexes = {
        @Index(name = "idx_task_project_status", columnList = "project_id,status"),
        @Index(name = "idx_task_due_date", columnList = "due_date")
})
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_task_project"))
    private Project project;
    @Column(nullable = false, length = 150)
    private String title;
    @Column(nullable = false, length = 1200)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", foreignKey = @ForeignKey(name = "fk_task_assignee"))
    private Employee assignee;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private TaskPriority priority;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private TaskStatus status;

    protected Task() {}
    public Task(Project project, String title, String description, Employee assignee, TaskPriority priority,
                LocalDate startDate, LocalDate dueDate, TaskStatus status) {
        this.project = project; this.title = title; this.description = description; this.assignee = assignee;
        this.priority = priority; this.startDate = startDate; this.dueDate = dueDate; this.status = status;
    }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Employee getAssignee() { return assignee; }
    public void setAssignee(Employee assignee) { this.assignee = assignee; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
