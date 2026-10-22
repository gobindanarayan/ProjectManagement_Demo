package com.manikagro.pms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_project_date", columnList = "project_id,expense_date"),
        @Index(name = "idx_expense_category", columnList = "category")
})
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_project"))
    private Project project;
    @Column(nullable = false, length = 150)
    private String title;
    @Column(nullable = false, length = 80)
    private String category;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
    @Column(nullable = false, length = 1200)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_recorder"))
    private Employee recordedBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Expense() {}
    public Expense(Project project, String title, String category, BigDecimal amount, LocalDate expenseDate, String description, Employee recordedBy) {
        this.project = project; this.title = title; this.category = category; this.amount = amount;
        this.expenseDate = expenseDate; this.description = description; this.recordedBy = recordedBy;
    }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Employee getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Employee recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
