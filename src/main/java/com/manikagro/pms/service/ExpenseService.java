package com.manikagro.pms.service;

import com.manikagro.pms.domain.Employee;
import com.manikagro.pms.domain.Expense;
import com.manikagro.pms.domain.Project;
import com.manikagro.pms.dto.ApiDtos.ExpenseRequest;
import com.manikagro.pms.dto.ApiDtos.ExpenseView;
import com.manikagro.pms.exception.ApiException;
import com.manikagro.pms.repository.ExpenseRepository;
import com.manikagro.pms.repository.ProjectRepository;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {
    private final ProjectRepository projects;
    private final ExpenseRepository expenses;
    private final ActivityService activities;
    private final DtoMapper mapper;
    public ExpenseService(ProjectRepository projects, ExpenseRepository expenses, ActivityService activities, DtoMapper mapper) {
        this.projects = projects; this.expenses = expenses; this.activities = activities; this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ExpenseView> list(Long projectId) {
        if (!projects.existsById(projectId)) throw ApiException.notFound("Project not found");
        return expenses.findByProjectIdOrderByExpenseDateDescIdDesc(projectId).stream().map(mapper::expense).toList();
    }

    @Transactional
    public ExpenseView create(Long projectId, ExpenseRequest request, Employee actor) {
        Project project = project(projectId);
        Expense expense = new Expense(project, request.title().trim(), request.category().trim(), request.amount().setScale(2), request.expenseDate(), request.description().trim(), actor);
        expense = expenses.save(expense);
        activities.record(project, actor, "EXPENSE_RECORDED", "Recorded " + request.category().trim() + " expense for " + project.getName());
        return mapper.expense(expense);
    }

    @Transactional
    public ExpenseView update(Long projectId, Long expenseId, ExpenseRequest request, Employee actor) {
        Project project = project(projectId);
        Expense expense = expenses.findById(expenseId).filter(e -> e.getProject().getId().equals(projectId))
                .orElseThrow(() -> ApiException.notFound("Expense record not found"));
        expense.setTitle(request.title().trim()); expense.setCategory(request.category().trim());
        expense.setAmount(request.amount().setScale(2, RoundingMode.UNNECESSARY)); expense.setExpenseDate(request.expenseDate()); expense.setDescription(request.description().trim());
        activities.record(project, actor, "EXPENSE_UPDATED", "Updated expense " + expense.getTitle());
        return mapper.expense(expense);
    }

    @Transactional
    public void delete(Long projectId, Long expenseId, Employee actor) {
        Project project = project(projectId);
        Expense expense = expenses.findById(expenseId).filter(e -> e.getProject().getId().equals(projectId))
                .orElseThrow(() -> ApiException.notFound("Expense record not found"));
        String title = expense.getTitle(); expenses.delete(expense);
        activities.record(project, actor, "EXPENSE_DELETED", "Deleted expense " + title);
    }

    private Project project(Long id) { return projects.findById(id).orElseThrow(() -> ApiException.notFound("Project not found")); }
}
