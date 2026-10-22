package com.manikagro.pms.controller;

import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.dto.ApiDtos.EmployeeView;
import com.manikagro.pms.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final EmployeeRepository employees;
    public AuthController(EmployeeRepository employees) { this.employees = employees; }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }

    @GetMapping("/me")
    public EmployeeView me(@AuthenticationPrincipal UserDetails user) {
        Employee employee = employees.findByUsernameIgnoreCase(user.getUsername()).orElseThrow();
        return EmployeeView.from(employee);
    }
}
