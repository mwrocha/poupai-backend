package io.poupai.backend.transaction.controller;

import io.poupai.backend.shared.response.ApiResponse;
import io.poupai.backend.transaction.dto.TransactionDtos;
import io.poupai.backend.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionDtos.TransactionResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        String userId = getUserId(userDetails);
        List<TransactionDtos.TransactionResponse> transactions;

        if (year != null && month != null) {
            transactions = transactionService.getByMonth(userId, year, month);
        } else {
            transactions = transactionService.getAll(userId);
        }

        return ResponseEntity.ok(ApiResponse.success("Transações recuperadas", transactions));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDtos.TransactionResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionDtos.CreateRequest request
    ) {
        String userId = getUserId(userDetails);
        TransactionDtos.TransactionResponse response = transactionService.create(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Transação criada", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        transactionService.delete(getUserId(userDetails), id);
        return ResponseEntity.ok(ApiResponse.success("Transação deletada", null));
    }

    // Por ora o userId é o próprio email — quando tiver campo ID separado, buscar do banco
    private String getUserId(UserDetails userDetails) {
        return userDetails.getUsername();
    }
}
