package com.manikagro.pms.repository;

import com.manikagro.pms.domain.Expense;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByProjectIdOrderByExpenseDateDescIdDesc(Long projectId);
    boolean existsByProjectId(Long projectId);
    boolean existsByRecordedById(Long employeeId);
    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.project.id = :projectId")
    BigDecimal totalByProjectId(@Param("projectId") Long projectId);
    @Query("select coalesce(sum(e.amount), 0) from Expense e")
    BigDecimal totalAll();
    @Query("select e.category, sum(e.amount) from Expense e where e.project.id = :projectId group by e.category order by sum(e.amount) desc")
    List<Object[]> totalsByCategory(@Param("projectId") Long projectId);
}
