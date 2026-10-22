package com.manikagro.pms.service;

import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.domain.EmployeeRole;
import com.manikagro.pms.dto.ApiDtos.EmployeeRequest;
import com.manikagro.pms.dto.ApiDtos.EmployeeView;
import com.manikagro.pms.exception.ApiException;
import com.manikagro.pms.repository.EmployeeRepository;
import com.manikagro.pms.repository.ProjectRepository;
import com.manikagro.pms.repository.ExpenseRepository;
import com.manikagro.pms.repository.ProjectMemberRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EmployeeService {
    private final EmployeeRepository employees;
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final ExpenseRepository expenses;
    private final PasswordEncoder encoder;
    public EmployeeService(EmployeeRepository employees, ProjectRepository projects, ProjectMemberRepository members,
                           ExpenseRepository expenses, PasswordEncoder encoder) {
        this.employees = employees; this.projects = projects; this.members = members; this.expenses = expenses; this.encoder = encoder;
    }
    @Transactional(readOnly = true)
    public List<EmployeeView> list() { return employees.findAllByOrderByNameAsc().stream().map(EmployeeView::from).toList(); }
    @Transactional(readOnly = true)
    public Employee get(Long id) { return employees.findById(id).orElseThrow(() -> ApiException.notFound("Employee not found")); }

    @Transactional
    public EmployeeView create(EmployeeRequest request) {
        if (!StringUtils.hasText(request.password())) throw ApiException.badRequest("Set an initial password with at least 12 characters");
        checkUnique(request, null);
        Employee employee = new Employee(normalize(request.employeeCode()), request.name().trim(), request.username().trim(), request.email().trim().toLowerCase(Locale.ROOT),
                encoder.encode(request.password()), request.jobTitle().trim(), request.role(), request.active());
        return EmployeeView.from(employees.save(employee));
    }

    @Transactional
    public EmployeeView update(Long id, EmployeeRequest request) {
        Employee employee = get(id); checkUnique(request, id);
        preventLastAdminRemoval(employee, request.active(), request.role());
        employee.setEmployeeCode(normalize(request.employeeCode())); employee.setName(request.name().trim());
        employee.setUsername(request.username().trim()); employee.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        employee.setJobTitle(request.jobTitle().trim()); employee.setRole(request.role()); employee.setActive(request.active());
        if (StringUtils.hasText(request.password())) employee.setPasswordHash(encoder.encode(request.password()));
        return EmployeeView.from(employee);
    }

    @Transactional
    public EmployeeView deactivate(Long id) {
        Employee employee = get(id); preventLastAdminRemoval(employee, false, employee.getRole()); employee.setActive(false); return EmployeeView.from(employee);
    }

    @Transactional(readOnly = true)
    public boolean isReferenced(Long employeeId) {
        return projects.existsByManagerId(employeeId) || members.existsByEmployeeIdAndActiveTrue(employeeId) || expenses.existsByRecordedById(employeeId);
    }

    private void checkUnique(EmployeeRequest request, Long currentId) {
        String code = normalize(request.employeeCode()); String username = request.username().trim(); String email = request.email().trim();
        if (employees.findByEmployeeCodeIgnoreCase(code).filter(e -> !e.getId().equals(currentId)).isPresent()) throw ApiException.conflict("Employee code is already in use");
        if (employees.findByUsernameIgnoreCase(username).filter(e -> !e.getId().equals(currentId)).isPresent()) throw ApiException.conflict("Username is already in use");
        if (employees.findByEmailIgnoreCase(email).filter(e -> !e.getId().equals(currentId)).isPresent()) throw ApiException.conflict("Email is already in use");
    }
    private void preventLastAdminRemoval(Employee employee, boolean willBeActive, EmployeeRole willHaveRole) {
        if (employee.isActive() && employee.getRole() == EmployeeRole.ADMIN
                && (!willBeActive || willHaveRole != EmployeeRole.ADMIN)
                && employees.countByRoleAndActiveTrue(EmployeeRole.ADMIN) <= 1) {
            throw ApiException.conflict("At least one active administrator must remain");
        }
    }
    private static String normalize(String value) { return value.trim().toUpperCase(Locale.ROOT); }
}
