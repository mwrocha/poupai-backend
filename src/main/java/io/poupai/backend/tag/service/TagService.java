package io.poupai.backend.tag.service;

import io.poupai.backend.tag.dto.TagDtos;
import io.poupai.backend.transaction.model.Transaction;
import io.poupai.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TransactionRepository transactionRepository;

    // Paleta de cores para categorias
    private static final List<String> COLORS = List.of(
            "#503173", "#7C5295", "#4CAF50", "#FF9800",
            "#E91E63", "#2196F3", "#009688", "#FF5722",
            "#795548", "#607D8B", "#9C27B0", "#F44336"
    );

    public TagDtos.TagsSummaryResponse getTagsSummary(String userId, Integer month, Integer year) {
        List<Transaction> transactions;

        if (month != null && year != null) {
            YearMonth ym = YearMonth.of(year, month);
            transactions = transactionRepository
                    .findByUserIdAndDateBetweenOrderByDateDesc(userId, ym.atDay(1), ym.atEndOfMonth());
        } else {
            transactions = transactionRepository.findByUserIdOrderByDateDesc(userId);
        }

        // Filtra apenas despesas para o agrupamento por categoria
        Map<String, List<Transaction>> grouped = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .filter(t -> t.getCategory() != null && !t.getCategory().isBlank())
                .collect(Collectors.groupingBy(t -> t.getCategory().trim()));

        // Ordena por total gasto (maior primeiro) e atribui cores
        List<String> sortedCategories = grouped.entrySet().stream()
                .sorted((a, b) -> Double.compare(
                        b.getValue().stream().mapToDouble(Transaction::getAmount).sum(),
                        a.getValue().stream().mapToDouble(Transaction::getAmount).sum()
                ))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<TagDtos.TagResponse> tags = new ArrayList<>();
        for (int i = 0; i < sortedCategories.size(); i++) {
            String category = sortedCategories.get(i);
            List<Transaction> categoryTransactions = grouped.get(category);
            double total = categoryTransactions.stream().mapToDouble(Transaction::getAmount).sum();
            String color = COLORS.get(i % COLORS.size());

            tags.add(TagDtos.TagResponse.builder()
                    .id(slugify(category))
                    .name(category)
                    .totalSpent(total)
                    .transactionCount((long) categoryTransactions.size())
                    .color(color)
                    .build());
        }

        double totalSpent = tags.stream().mapToDouble(TagDtos.TagResponse::getTotalSpent).sum();

        return TagDtos.TagsSummaryResponse.builder()
                .tags(tags)
                .totalSpent(totalSpent)
                .month(month)
                .year(year)
                .build();
    }

    private String slugify(String text) {
        return text.toLowerCase()
                .replaceAll("[áàãâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòõôö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
}