package com.manikagro.pms.controller;

import com.manikagro.pms.dto.ApiDtos.EmployeeRequest;
import com.manikagro.pms.dto.ApiDtos.EmployeeView;
import com.manikagro.pms.service.EmployeeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {
    private final EmployeeService employees;
    public EmployeeController(EmployeeService employees) { this.employees = employees; }
    @GetMapping public List<EmployeeView> list() { return employees.list(); }
    @PostMapping public ResponseEntity<EmployeeView> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeView employee = employees.create(request); return ResponseEntity.created(URI.create("/api/employees/" + employee.id())).body(employee);
    }
    @PutMapping("/{id}") public EmployeeView update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) { return employees.update(id, request); }
    @DeleteMapping("/{id}") public EmployeeView deactivate(@PathVariable Long id) { return employees.deactivate(id); }
}
