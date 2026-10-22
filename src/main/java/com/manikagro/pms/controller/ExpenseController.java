package com.manikagro.pms.controller;

import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.dto.ApiDtos.ExpenseRequest;
import com.manikagro.pms.dto.ApiDtos.ExpenseView;
import com.manikagro.pms.exception.ApiException;
import com.manikagro.pms.repository.EmployeeRepository;
import com.manikagro.pms.service.ExpenseService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/expenses")
public class ExpenseController {
    private final ExpenseService expenses;
    private final EmployeeRepository employees;
    public ExpenseController(ExpenseService expenses, EmployeeRepository employees) { this.expenses = expenses; this.employees = employees; }
    @GetMapping public List<ExpenseView> list(@PathVariable Long projectId) { return expenses.list(projectId); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExpenseView> create(@PathVariable Long projectId, @Valid @RequestBody ExpenseRequest request, @AuthenticationPrincipal UserDetails user) {
        ExpenseView result = expenses.create(projectId, request, actor(user)); return ResponseEntity.created(URI.create("/api/projects/" + projectId + "/expenses/" + result.id())).body(result);
    }
    @PutMapping("/{expenseId}") @PreAuthorize("hasRole('ADMIN')")
    public ExpenseView update(@PathVariable Long projectId, @PathVariable Long expenseId, @Valid @RequestBody ExpenseRequest request, @AuthenticationPrincipal UserDetails user) {
        return expenses.update(projectId, expenseId, request, actor(user));
    }
    @DeleteMapping("/{expenseId}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long expenseId, @AuthenticationPrincipal UserDetails user) {
        expenses.delete(projectId, expenseId, actor(user)); return ResponseEntity.noContent().build();
    }
    private Employee actor(UserDetails user) { return employees.findByUsernameIgnoreCase(user.getUsername()).orElseThrow(() -> ApiException.notFound("Employee account not found")); }
}
