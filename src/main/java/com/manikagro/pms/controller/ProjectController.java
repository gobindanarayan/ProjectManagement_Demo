package com.manikagro.pms.controller;

import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.domain.ProjectStatus;
import com.manikagro.pms.dto.ApiDtos.*;
import com.manikagro.pms.exception.ApiException;
import com.manikagro.pms.repository.EmployeeRepository;
import com.manikagro.pms.service.ProjectService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projects;
    private final EmployeeRepository employees;
    public ProjectController(ProjectService projects, EmployeeRepository employees) { this.projects = projects; this.employees = employees; }

    @GetMapping public List<ProjectView> list(@RequestParam(required = false) String search, @RequestParam(required = false) ProjectStatus status) {
        return projects.list(search, status);
    }
    @GetMapping("/{id}") public ProjectDetail detail(@PathVariable Long id) { return projects.detail(id); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectView> create(@Valid @RequestBody ProjectRequest request, @AuthenticationPrincipal UserDetails user) {
        ProjectView created = projects.create(request, actor(user)); return ResponseEntity.created(URI.create("/api/projects/" + created.id())).body(created);
    }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ProjectView update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request, @AuthenticationPrincipal UserDetails user) {
        return projects.update(id, request, actor(user));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        projects.delete(id, actor(user)); return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/members") @PreAuthorize("hasRole('ADMIN')")
    public ProjectMemberView addMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request, @AuthenticationPrincipal UserDetails user) {
        return projects.addMember(id, request, actor(user));
    }
    @DeleteMapping("/{id}/members/{employeeId}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long employeeId, @AuthenticationPrincipal UserDetails user) {
        projects.deactivateMember(id, employeeId, actor(user)); return ResponseEntity.noContent().build();
    }
    private Employee actor(UserDetails user) {
        return employees.findByUsernameIgnoreCase(user.getUsername()).orElseThrow(() -> ApiException.notFound("Employee account not found"));
    }
}
