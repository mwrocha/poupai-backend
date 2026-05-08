package io.poupai.backend.finance.service;

import io.poupai.backend.finance.dto.FinanceDtos;
import io.poupai.backend.transaction.model.Transaction;
import io.poupai.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final TransactionRepository transactionRepository;

    public FinanceDtos.FinanceSummaryResponse getSummary(String userId, int months) {
        List<Double> incomeHistory = new ArrayList<>();
        List<Double> expenseHistory = new ArrayList<>();
        List<Double> profitHistory = new ArrayList<>();

        // Calcular os últimos N meses
        YearMonth current = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = current.minusMonths(i);
            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.atEndOfMonth();

            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);

            double income = transactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double expense = transactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            incomeHistory.add(income);
            expenseHistory.add(expense);
            profitHistory.add(income - expense);
        }

        return FinanceDtos.FinanceSummaryResponse.builder()
                .incomeHistory(incomeHistory)
                .expenseHistory(expenseHistory)
                .profitHistory(profitHistory)
                .build();
    }
}
