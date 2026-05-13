package io.poupai.backend.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class FinanceDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinanceSummaryResponse {
        // Histórico mensal
        private List<Double> incomeHistory;
        private List<Double> expenseHistory;
        private List<Double> profitHistory;
        private List<String> monthLabels;

        // Totais do período
        private Double totalIncome;
        private Double totalExpense;
        private Double totalProfit;

        // Comparativo com período anterior
        private Double incomeChangePercent;
        private Double expenseChangePercent;
        private Double profitChangePercent;

        // Médias
        private Double avgDailyExpense;
        private Double avgMonthlyExpense;

        // Projeção do mês atual
        private Double projectedMonthlyExpense;

        // Maior gasto
        private String biggestExpenseTitle;
        private Double biggestExpenseAmount;

        // Distribuição por categoria
        private List<CategoryBreakdown> categoryBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private Double total;
        private Double percent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodRequest {
        private Integer month;
        private Integer year;
        private Integer months;
    }
}