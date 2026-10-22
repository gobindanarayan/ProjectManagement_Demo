package com.manikagro.pms.service;

import com.manikagro.pms.domain.*;
import com.manikagro.pms.dto.ApiDtos.TaskRequest;
import com.manikagro.pms.dto.ApiDtos.TaskView;
import com.manikagro.pms.exception.ApiException;
import com.manikagro.pms.repository.EmployeeRepository;
import com.manikagro.pms.repository.ProjectMemberRepository;
import com.manikagro.pms.repository.ProjectRepository;
import com.manikagro.pms.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final EmployeeRepository employees;
    private final ProjectMemberRepository members;
    private final ActivityService activities;
    private final DtoMapper mapper;
    public TaskService(ProjectRepository projects, TaskRepository tasks, EmployeeRepository employees,
                       ProjectMemberRepository members, ActivityService activities, DtoMapper mapper) {
        this.projects = projects; this.tasks = tasks; this.employees = employees; this.members = members; this.activities = activities; this.mapper = mapper;
    }

    @Transactional
    public TaskView create(Long projectId, TaskRequest request, Employee actor) {
        Project project = project(projectId); validateDates(request.startDate(), request.dueDate());
        Task task = new Task(project, request.title().trim(), request.description().trim(), assignee(projectId, request.assigneeId()),
                request.priority(), request.startDate(), request.dueDate(), request.status());
        task = tasks.save(task);
        activities.record(project, actor, "TASK_CREATED", "Added task " + task.getTitle());
        return mapper.task(task);
    }

    @Transactional
    public TaskView update(Long projectId, Long taskId, TaskRequest request, Employee actor) {
        Project project = project(projectId); validateDates(request.startDate(), request.dueDate());
        Task task = tasks.findById(taskId).filter(t -> t.getProject().getId().equals(projectId))
                .orElseThrow(() -> ApiException.notFound("Task not found"));
        task.setTitle(request.title().trim()); task.setDescription(request.description().trim()); task.setAssignee(assignee(projectId, request.assigneeId()));
        task.setPriority(request.priority()); task.setStartDate(request.startDate()); task.setDueDate(request.dueDate()); task.setStatus(request.status());
        activities.record(project, actor, "TASK_UPDATED", "Updated task " + task.getTitle());
        return mapper.task(task);
    }

    @Transactional
    public void delete(Long projectId, Long taskId, Employee actor) {
        Project project = project(projectId);
        Task task = tasks.findById(taskId).filter(t -> t.getProject().getId().equals(projectId))
                .orElseThrow(() -> ApiException.notFound("Task not found"));
        String title = task.getTitle(); tasks.delete(task);
        activities.record(project, actor, "TASK_DELETED", "Deleted task " + title);
    }

    private Project project(Long id) { return projects.findById(id).orElseThrow(() -> ApiException.notFound("Project not found")); }
    private Employee assignee(Long projectId, Long employeeId) {
        if (employeeId == null) return null;
        Employee employee = employees.findById(employeeId).orElseThrow(() -> ApiException.notFound("Employee not found"));
        ProjectMember member = members.findByProjectIdAndEmployeeId(projectId, employeeId).orElse(null);
        if (!employee.isActive() || member == null || !member.isActive()) throw ApiException.badRequest("The assignee must be an active member of this project");
        return employee;
    }
    private static void validateDates(LocalDate start, LocalDate due) {
        if (due.isBefore(start)) throw ApiException.badRequest("Task due date must be on or after its start date");
    }
}
