package com.manikagro.pms.service;

import com.manikagro.pms.domain.*;
import com.manikagro.pms.dto.ApiDtos.*;
import com.manikagro.pms.exception.ApiException;
import com.manikagro.pms.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final EmployeeRepository employees;
    private final ProjectMemberRepository members;
    private final TaskRepository tasks;
    private final ExpenseRepository expenses;
    private final ActivityLogRepository logs;
    private final ActivityService activities;
    private final DtoMapper mapper;

    public ProjectService(ProjectRepository projects, EmployeeRepository employees, ProjectMemberRepository members,
                          TaskRepository tasks, ExpenseRepository expenses, ActivityLogRepository logs,
                          ActivityService activities, DtoMapper mapper) {
        this.projects = projects; this.employees = employees; this.members = members; this.tasks = tasks;
        this.expenses = expenses; this.logs = logs; this.activities = activities; this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProjectView> list(String query, ProjectStatus status) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return projects.findAllByOrderByUpdatedAtDesc().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> needle.isEmpty() || p.getName().toLowerCase(Locale.ROOT).contains(needle) || p.getCode().toLowerCase(Locale.ROOT).contains(needle))
                .map(mapper::project).toList();
    }

    @Transactional(readOnly = true)
    public Project get(Long id) { return projects.findById(id).orElseThrow(() -> ApiException.notFound("Project not found")); }

    @Transactional(readOnly = true)
    public ProjectDetail detail(Long id) {
        Project project = get(id);
        List<CategoryTotal> byCategory = expenses.totalsByCategory(id).stream()
                .map(row -> new CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();
        return mapper.detail(project, members.findByProjectIdOrderByAssignedDateAsc(id), tasks.findByProjectIdOrderByDueDateAsc(id),
                expenses.findByProjectIdOrderByExpenseDateDescIdDesc(id), byCategory);
    }

    @Transactional
    public ProjectView create(ProjectRequest request, Employee actor) {
        validateDates(request.startDate(), request.expectedCompletionDate());
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (projects.existsByCodeIgnoreCase(code)) throw ApiException.conflict("A project with this code already exists");
        Employee manager = activeEmployee(request.managerId());
        Project project = projects.save(new Project(code, request.name().trim(), request.description().trim(), manager,
                money(request.approvedBudget()), request.startDate(), request.expectedCompletionDate(), request.status(), request.demoData()));
        activities.record(project, actor, "PROJECT_CREATED", "Created project " + project.getName());
        return mapper.project(project);
    }

    @Transactional
    public ProjectView update(Long id, ProjectRequest request, Employee actor) {
        validateDates(request.startDate(), request.expectedCompletionDate());
        Project project = get(id);
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (!project.getCode().equalsIgnoreCase(code) && projects.existsByCodeIgnoreCase(code)) throw ApiException.conflict("A project with this code already exists");
        project.setCode(code); project.setName(request.name().trim()); project.setDescription(request.description().trim());
        project.setManager(activeEmployee(request.managerId())); project.setApprovedBudget(money(request.approvedBudget()));
        project.setStartDate(request.startDate()); project.setExpectedCompletionDate(request.expectedCompletionDate()); project.setStatus(request.status());
        project.setDemoData(request.demoData());
        activities.record(project, actor, "PROJECT_UPDATED", "Updated project " + project.getName());
        return mapper.project(project);
    }

    @Transactional
    public void delete(Long id, Employee actor) {
        Project project = get(id);
        if (expenses.existsByProjectId(id)) throw ApiException.conflict("This project has expense history and cannot be deleted. Set its status to Cancelled instead.");
        tasks.deleteAll(tasks.findByProjectIdOrderByDueDateAsc(id));
        members.deleteAll(members.findByProjectIdOrderByAssignedDateAsc(id));
        logs.findByProjectId(id).forEach(log -> log.setProject(null));
        activities.record(null, actor, "PROJECT_DELETED", "Deleted project " + project.getName());
        projects.delete(project);
    }

    @Transactional
    public ProjectMemberView addMember(Long projectId, MemberRequest request, Employee actor) {
        Project project = get(projectId);
        Employee employee = activeEmployee(request.employeeId());
        ProjectMember member = members.findByProjectIdAndEmployeeId(projectId, employee.getId()).orElse(null);
        if (member == null) member = new ProjectMember(project, employee, request.projectRole().trim(), request.assignedDate(), request.active());
        else { member.setProjectRole(request.projectRole().trim()); member.setAssignedDate(request.assignedDate()); member.setActive(request.active()); }
        member = members.save(member);
        activities.record(project, actor, "MEMBER_UPDATED", "Updated team member " + employee.getName());
        return mapper.member(member);
    }

    @Transactional
    public void deactivateMember(Long projectId, Long employeeId, Employee actor) {
        Project project = get(projectId);
        ProjectMember member = members.findByProjectIdAndEmployeeId(projectId, employeeId)
                .orElseThrow(() -> ApiException.notFound("Project member not found"));
        member.setActive(false);
        activities.record(project, actor, "MEMBER_UPDATED", "Removed " + member.getEmployee().getName() + " from the active team");
    }

    private Employee activeEmployee(Long id) {
        Employee employee = employees.findById(id).orElseThrow(() -> ApiException.notFound("Employee not found"));
        if (!employee.isActive()) throw ApiException.badRequest("Select an active employee");
        return employee;
    }
    private static void validateDates(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) throw ApiException.badRequest("Expected completion date must be on or after the start date");
    }
    private static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() < 0) throw ApiException.badRequest("Budget must be zero or greater");
        return value.setScale(2, java.math.RoundingMode.UNNECESSARY);
    }
}
