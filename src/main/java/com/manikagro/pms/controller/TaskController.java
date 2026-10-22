package com.manikagro.pms.controller;

import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.dto.ApiDtos.TaskRequest;
import com.manikagro.pms.dto.ApiDtos.TaskView;
import com.manikagro.pms.exception.ApiException;
import com.manikagro.pms.repository.EmployeeRepository;
import com.manikagro.pms.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {
    private final TaskService tasks;
    private final EmployeeRepository employees;
    public TaskController(TaskService tasks, EmployeeRepository employees) { this.tasks = tasks; this.employees = employees; }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public TaskView create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request, @AuthenticationPrincipal UserDetails user) {
        return tasks.create(projectId, request, actor(user));
    }
    @PutMapping("/{taskId}") @PreAuthorize("hasRole('ADMIN')")
    public TaskView update(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody TaskRequest request, @AuthenticationPrincipal UserDetails user) {
        return tasks.update(projectId, taskId, request, actor(user));
    }
    @DeleteMapping("/{taskId}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long taskId, @AuthenticationPrincipal UserDetails user) {
        tasks.delete(projectId, taskId, actor(user)); return ResponseEntity.noContent().build();
    }
    private Employee actor(UserDetails user) { return employees.findByUsernameIgnoreCase(user.getUsername()).orElseThrow(() -> ApiException.notFound("Employee account not found")); }
}
