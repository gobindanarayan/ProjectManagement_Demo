package com.manikagro.pms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_active", columnList = "active")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_code", columnNames = "employee_code"),
        @UniqueConstraint(name = "uk_employee_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_employee_email", columnNames = "email")
})
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "employee_code", nullable = false, length = 32)
    private String employeeCode;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 80)
    private String username;
    @Column(nullable = false, length = 190)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EmployeeRole role;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Employee() {}

    public Employee(String employeeCode, String name, String username, String email, String passwordHash,
                    String jobTitle, EmployeeRole role, boolean active) {
        this.employeeCode = employeeCode; this.name = name; this.username = username; this.email = email;
        this.passwordHash = passwordHash; this.jobTitle = jobTitle; this.role = role; this.active = active;
    }

    public Long getId() { return id; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public EmployeeRole getRole() { return role; }
    public void setRole(EmployeeRole role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
