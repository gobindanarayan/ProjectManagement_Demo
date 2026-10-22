package com.manikagro.pms.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_members", uniqueConstraints = @UniqueConstraint(name = "uk_project_member", columnNames = {"project_id", "employee_id"}),
       indexes = @Index(name = "idx_member_project_active", columnList = "project_id,active"))
public class ProjectMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_member_project"))
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_member_employee"))
    private Employee employee;
    @Column(name = "project_role", nullable = false, length = 100)
    private String projectRole;
    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;
    @Column(nullable = false)
    private boolean active = true;

    protected ProjectMember() {}
    public ProjectMember(Project project, Employee employee, String projectRole, LocalDate assignedDate, boolean active) {
        this.project = project; this.employee = employee; this.projectRole = projectRole; this.assignedDate = assignedDate; this.active = active;
    }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public Employee getEmployee() { return employee; }
    public String getProjectRole() { return projectRole; }
    public void setProjectRole(String projectRole) { this.projectRole = projectRole; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
