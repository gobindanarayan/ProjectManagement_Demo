package com.manikagro.pms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_project_time", columnList = "project_id,occurred_at"),
        @Index(name = "idx_activity_time", columnList = "occurred_at")
})
public class ActivityLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_activity_project"))
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_activity_actor"))
    private Employee actor;
    @Column(nullable = false, length = 60)
    private String action;
    @Column(nullable = false, length = 500)
    private String description;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    protected ActivityLog() {}
    public ActivityLog(Project project, Employee actor, String action, String description) {
        this.project = project; this.actor = actor; this.action = action; this.description = description;
    }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public Employee getActor() { return actor; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
