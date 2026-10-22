package com.manikagro.pms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.domain.EmployeeRole;
import com.manikagro.pms.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjectApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired EmployeeRepository employees;
    @Autowired PasswordEncoder encoder;
    private Employee admin;

    @BeforeEach
    void createAdmin() {
        admin = employees.findByUsernameIgnoreCase("test-admin").orElseGet(() -> employees.save(
                new Employee("TEST-ADMIN", "Test Administrator", "test-admin", "test-admin@example.invalid",
                        encoder.encode("test-only-password-123"), "Administrator", EmployeeRole.ADMIN, true)));
    }

    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void expenseUpdatesPersistedProjectTotals() throws Exception {
        long managerId = employees.findByEmployeeCodeIgnoreCase("DEMO-EMP-01").orElseThrow().getId();
        String projectJson = json.writeValueAsString(Map.of(
                "name", "Integration test project", "code", "TEST-FIN-1", "description", "Project used by an automated integration test.",
                "managerId", managerId, "approvedBudget", new BigDecimal("10000.00"), "startDate", LocalDate.now(),
                "expectedCompletionDate", LocalDate.now().plusMonths(2), "status", "ACTIVE", "demoData", true));
        String response = mvc.perform(post("/api/projects").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(projectJson))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("TEST-FIN-1")).andReturn().getResponse().getContentAsString();
        long projectId = json.readTree(response).get("id").asLong();

        String expenseJson = json.writeValueAsString(Map.of("title", "Test travel", "category", "Travel", "amount", new BigDecimal("1250.50"),
                "expenseDate", LocalDate.now(), "description", "Automated test expense."));
        mvc.perform(post("/api/projects/{id}/expenses", projectId).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(expenseJson))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.expenditure").value(1250.50))
                .andExpect(jsonPath("$.project.remainingBudget").value(8749.50))
                .andExpect(jsonPath("$.project.budgetUtilization").value(12.51));

        long memberId = employees.findByEmployeeCodeIgnoreCase("DEMO-EMP-02").orElseThrow().getId();
        String memberJson = json.writeValueAsString(Map.of("employeeId", memberId, "projectRole", "Coordinator",
                "assignedDate", LocalDate.now(), "active", true));
        mvc.perform(post("/api/projects/{id}/members", projectId).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(memberJson))
                .andExpect(status().isOk());

        String taskJson = json.writeValueAsString(Map.of("title", "Test milestone", "description", "Task API integration test.",
                "assigneeId", memberId, "priority", "MEDIUM", "startDate", LocalDate.now(),
                "dueDate", LocalDate.now().plusDays(1), "status", "TO_DO"));
        String taskResponse = mvc.perform(post("/api/projects/{id}/tasks", projectId).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(taskJson))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long taskId = json.readTree(taskResponse).get("id").asLong();
        mvc.perform(get("/api/projects/{id}", projectId)).andExpect(jsonPath("$.project.progress").value(0));

        String completedTaskJson = json.writeValueAsString(Map.of("title", "Test milestone", "description", "Task API integration test.",
                "assigneeId", memberId, "priority", "MEDIUM", "startDate", LocalDate.now(),
                "dueDate", LocalDate.now().plusDays(1), "status", "COMPLETED"));
        mvc.perform(put("/api/projects/{id}/tasks/{taskId}", projectId, taskId).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(completedTaskJson))
                .andExpect(status().isOk());
        mvc.perform(get("/api/projects/{id}", projectId)).andExpect(jsonPath("$.project.progress").value(100));
        mvc.perform(delete("/api/projects/{id}/tasks/{taskId}", projectId, taskId).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/projects/{id}", projectId)).andExpect(jsonPath("$.project.progress").value(0));
    }

    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void duplicateProjectCodesAreRejected() throws Exception {
        long managerId = employees.findByEmployeeCodeIgnoreCase("DEMO-EMP-01").orElseThrow().getId();
        Map<String, Object> data = Map.of("name", "Duplicate test", "code", "DUP-TEST", "description", "Duplicate code test.",
                "managerId", managerId, "approvedBudget", 0, "startDate", LocalDate.now(),
                "expectedCompletionDate", LocalDate.now().plusDays(1), "status", "PLANNING", "demoData", true);
        String body = json.writeValueAsString(data);
        mvc.perform(post("/api/projects").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mvc.perform(post("/api/projects").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("A project with this code already exists"));
    }

    @Test
    @WithMockUser(username = "test-admin", roles = "EMPLOYEE")
    void employeesCannotCreateProjects() throws Exception {
        long managerId = employees.findByEmployeeCodeIgnoreCase("DEMO-EMP-01").orElseThrow().getId();
        String body = json.writeValueAsString(Map.of("name", "Forbidden project", "code", "FORBIDDEN-1", "description", "Valid request body.",
                "managerId", managerId, "approvedBudget", 100, "startDate", LocalDate.now(),
                "expectedCompletionDate", LocalDate.now().plusDays(1), "status", "ACTIVE", "demoData", true));
        mvc.perform(post("/api/projects").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginCreatesAnAuthenticatedSession() throws Exception {
        var login = mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "test-admin")
                        .param("password", "test-only-password-123"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test-admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
        mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-admin", roles = "EMPLOYEE")
    void dashboardTotalsAndActivityPagesComeFromSeededRecords() throws Exception {
        String dashboardJson = mvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.totalProjects").value(3))
                .andExpect(jsonPath("$.stats.totalBudget").value(2130000.00))
                .andExpect(jsonPath("$.stats.totalExpenditure").value(747450.00))
                .andExpect(jsonPath("$.stats.remainingBudget").value(1382550.00))
                .andReturn().getResponse().getContentAsString();
        assertFalse(dashboardJson.contains("\"email\""));
        assertFalse(dashboardJson.contains("\"username\""));

        mvc.perform(get("/api/activity").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void frontendFilesAreServedByTheApplication() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk());
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Manik Agro | Project Workspace")));
        mvc.perform(get("/assets/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/dashboard")));
    }
}
