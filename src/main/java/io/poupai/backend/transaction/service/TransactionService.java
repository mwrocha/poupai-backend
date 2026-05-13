package io.poupai.backend.transaction.service;

import io.poupai.backend.gamification.service.GamificationService;
import io.poupai.backend.transaction.dto.TransactionDtos;
import io.poupai.backend.transaction.model.Transaction;
import io.poupai.backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final GamificationService gamificationService;

    public List<TransactionDtos.TransactionResponse> getAll(String userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TransactionDtos.TransactionResponse> getByMonth(String userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        userId, yearMonth.atDay(1), yearMonth.atEndOfMonth())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TransactionDtos.TransactionResponse create(String userId, TransactionDtos.CreateRequest request) {
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .title(request.getTitle())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(LocalDate.parse(request.getDate()))
                .tagId(request.getTagId())
                .build();

        TransactionDtos.TransactionResponse response = toResponse(transactionRepository.save(transaction));
        gamificationService.onTransactionCreated(userId);
        return response;
    }

    public TransactionDtos.TransactionResponse update(String userId, String transactionId, TransactionDtos.UpdateRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Sem permissão para editar esta transação");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            transaction.setTitle(request.getTitle());
        }
        if (request.getAmount() != null && request.getAmount() > 0) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            transaction.setCategory(request.getCategory());
        }
        if (request.getDate() != null && !request.getDate().isBlank()) {
            transaction.setDate(LocalDate.parse(request.getDate()));
        }

        return toResponse(transactionRepository.save(transaction));
    }

    public void delete(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Sem permissão para deletar esta transação");
        }
        transactionRepository.delete(transaction);
    }

    private TransactionDtos.TransactionResponse toResponse(Transaction t) {
        return TransactionDtos.TransactionResponse.builder()
                .id(t.getId()).title(t.getTitle()).amount(t.getAmount())
                .type(t.getType().name()).category(t.getCategory())
                .date(t.getDate().toString()).tagId(t.getTagId())
                .build();
    }
}