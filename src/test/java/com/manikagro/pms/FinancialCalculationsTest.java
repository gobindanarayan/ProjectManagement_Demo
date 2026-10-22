package com.manikagro.pms;

import com.manikagro.pms.service.FinancialCalculations;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialCalculationsTest {
    @Test
    void zeroBudgetReturnsSafeUtilizationWithoutDivisionByZero() {
        assertEquals(0.0, FinancialCalculations.utilizationPercent(BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(100.0, FinancialCalculations.utilizationPercent(BigDecimal.ZERO, new BigDecimal("25.00")));
    }

    @Test
    void remainingBudgetCanShowOverspendAndNullAmountsAreSafe() {
        assertEquals(new BigDecimal("-5.00"), FinancialCalculations.remainingBudget(new BigDecimal("95.00"), new BigDecimal("100.00")));
        assertEquals(BigDecimal.ZERO, FinancialCalculations.remainingBudget(null, null));
    }

    @Test
    void utilizationUsesTwoDecimalPrecision() {
        assertEquals(12.51, FinancialCalculations.utilizationPercent(new BigDecimal("10000.00"), new BigDecimal("1250.50")));
    }
}
