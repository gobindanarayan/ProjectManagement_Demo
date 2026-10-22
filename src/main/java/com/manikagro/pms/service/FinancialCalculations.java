package com.manikagro.pms.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialCalculations {
    private FinancialCalculations() {}

    public static BigDecimal safe(BigDecimal amount) { return amount == null ? BigDecimal.ZERO : amount; }

    public static BigDecimal remainingBudget(BigDecimal approvedBudget, BigDecimal expenditure) {
        return safe(approvedBudget).subtract(safe(expenditure));
    }

    public static double utilizationPercent(BigDecimal approvedBudget, BigDecimal expenditure) {
        BigDecimal budget = safe(approvedBudget);
        BigDecimal spent = safe(expenditure);
        if (budget.signum() == 0) return spent.signum() > 0 ? 100.0 : 0.0;
        return spent.multiply(BigDecimal.valueOf(100)).divide(budget, 2, RoundingMode.HALF_UP).doubleValue();
    }
}
