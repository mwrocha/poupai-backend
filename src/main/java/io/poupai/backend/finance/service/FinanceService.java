package io.poupai.backend.finance.service;

import io.poupai.backend.finance.dto.FinanceDtos;
import io.poupai.backend.transaction.model.Transaction;
import io.poupai.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final TransactionRepository transactionRepository;

    // ─── Resumo por número de meses (endpoint original, expandido) ───

    public FinanceDtos.FinanceSummaryResponse getSummary(String userId, int months) {
        List<Double> incomeHistory = new ArrayList<>();
        List<Double> expenseHistory = new ArrayList<>();
        List<Double> profitHistory = new ArrayList<>();
        List<String> monthLabels = new ArrayList<>();

        YearMonth current = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = current.minusMonths(i);
            MonthTotals totals = getMonthTotals(userId, yearMonth);

            incomeHistory.add(totals.income);
            expenseHistory.add(totals.expense);
            profitHistory.add(totals.income - totals.expense);
            monthLabels.add(yearMonth.getMonth()
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")));
        }

        // Totais do período completo
        double totalIncome = incomeHistory.stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = expenseHistory.stream().mapToDouble(Double::doubleValue).sum();
        double totalProfit = totalIncome - totalExpense;

        // Comparativo com período anterior (mesmo número de meses antes)
        double prevIncome = 0, prevExpense = 0;
        for (int i = months * 2 - 1; i >= months; i--) {
            YearMonth yearMonth = current.minusMonths(i);
            MonthTotals totals = getMonthTotals(userId, yearMonth);
            prevIncome += totals.income;
            prevExpense += totals.expense;
        }
        double prevProfit = prevIncome - prevExpense;

        double incomeChangePercent = prevIncome > 0 ? ((totalIncome - prevIncome) / prevIncome) * 100 : 0;
        double expenseChangePercent = prevExpense > 0 ? ((totalExpense - prevExpense) / prevExpense) * 100 : 0;
        double profitChangePercent = prevProfit != 0 ? ((totalProfit - prevProfit) / Math.abs(prevProfit)) * 100 : 0;

        // Média diária de gastos (dias corridos do período)
        int totalDays = months * 30;
        double avgDailyExpense = totalDays > 0 ? totalExpense / totalDays : 0;
        double avgMonthlyExpense = months > 0 ? totalExpense / months : 0;

        // Projeção do mês atual
        double projectedMonthlyExpense = projectCurrentMonth(userId, current);

        // Maior gasto do período
        LocalDate periodStart = current.minusMonths(months - 1).atDay(1);
        LocalDate periodEnd = current.atEndOfMonth();
        List<Transaction> allTransactions = transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, periodStart, periodEnd);

        Transaction biggestExpense = allTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .max(Comparator.comparingDouble(Transaction::getAmount))
                .orElse(null);

        // Distribuição por categoria
        List<FinanceDtos.CategoryBreakdown> categoryBreakdown =
                buildCategoryBreakdown(allTransactions, totalExpense);

        return FinanceDtos.FinanceSummaryResponse.builder()
                .incomeHistory(incomeHistory)
                .expenseHistory(expenseHistory)
                .profitHistory(profitHistory)
                .monthLabels(monthLabels)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalProfit(totalProfit)
                .incomeChangePercent(round(incomeChangePercent))
                .expenseChangePercent(round(expenseChangePercent))
                .profitChangePercent(round(profitChangePercent))
                .avgDailyExpense(round(avgDailyExpense))
                .avgMonthlyExpense(round(avgMonthlyExpense))
                .projectedMonthlyExpense(round(projectedMonthlyExpense))
                .biggestExpenseTitle(biggestExpense != null ? biggestExpense.getTitle() : null)
                .biggestExpenseAmount(biggestExpense != null ? biggestExpense.getAmount() : null)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    // ─── Resumo por mês/ano específico ───

    public FinanceDtos.FinanceSummaryResponse getSummaryByPeriod(
            String userId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        YearMonth prevYearMonth = yearMonth.minusMonths(1);

        MonthTotals current = getMonthTotals(userId, yearMonth);
        MonthTotals previous = getMonthTotals(userId, prevYearMonth);

        double totalIncome = current.income;
        double totalExpense = current.expense;
        double totalProfit = totalIncome - totalExpense;

        double incomeChangePercent = previous.income > 0
                ? ((totalIncome - previous.income) / previous.income) * 100 : 0;
        double expenseChangePercent = previous.expense > 0
                ? ((totalExpense - previous.expense) / previous.expense) * 100 : 0;
        double prevProfit = previous.income - previous.expense;
        double profitChangePercent = prevProfit != 0
                ? ((totalProfit - prevProfit) / Math.abs(prevProfit)) * 100 : 0;

        int daysInMonth = yearMonth.lengthOfMonth();
        double avgDailyExpense = daysInMonth > 0 ? totalExpense / daysInMonth : 0;

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);

        Transaction biggestExpense = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .max(Comparator.comparingDouble(Transaction::getAmount))
                .orElse(null);

        List<FinanceDtos.CategoryBreakdown> categoryBreakdown =
                buildCategoryBreakdown(transactions, totalExpense);

        // Projeção só faz sentido para o mês atual
        double projectedMonthlyExpense = yearMonth.equals(YearMonth.now())
                ? projectCurrentMonth(userId, yearMonth)
                : totalExpense;

        return FinanceDtos.FinanceSummaryResponse.builder()
                .incomeHistory(List.of(current.income))
                .expenseHistory(List.of(current.expense))
                .profitHistory(List.of(totalProfit))
                .monthLabels(List.of(yearMonth.getMonth()
                        .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))))
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalProfit(totalProfit)
                .incomeChangePercent(round(incomeChangePercent))
                .expenseChangePercent(round(expenseChangePercent))
                .profitChangePercent(round(profitChangePercent))
                .avgDailyExpense(round(avgDailyExpense))
                .avgMonthlyExpense(round(totalExpense))
                .projectedMonthlyExpense(round(projectedMonthlyExpense))
                .biggestExpenseTitle(biggestExpense != null ? biggestExpense.getTitle() : null)
                .biggestExpenseAmount(biggestExpense != null ? biggestExpense.getAmount() : null)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    // ─── Helpers ───

    private MonthTotals getMonthTotals(String userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);

        double income = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
        double expense = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        return new MonthTotals(income, expense);
    }

    private double projectCurrentMonth(String userId, YearMonth current) {
        LocalDate start = current.atDay(1);
        LocalDate today = LocalDate.now();
        List<Transaction> soFar = transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, start, today);

        double spentSoFar = soFar.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        int daysPassed = today.getDayOfMonth();
        int daysInMonth = current.lengthOfMonth();

        return daysPassed > 0 ? (spentSoFar / daysPassed) * daysInMonth : 0;
    }

    private List<FinanceDtos.CategoryBreakdown> buildCategoryBreakdown(
            List<Transaction> transactions, double totalExpense) {
        Map<String, Double> byCategory = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Outros",
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        return byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> FinanceDtos.CategoryBreakdown.builder()
                        .category(e.getKey())
                        .total(round(e.getValue()))
                        .percent(totalExpense > 0 ? round((e.getValue() / totalExpense) * 100) : 0)
                        .build())
                .collect(Collectors.toList());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class MonthTotals {
        final double income;
        final double expense;
        MonthTotals(double income, double expense) {
            this.income = income;
            this.expense = expense;
        }
    }
}