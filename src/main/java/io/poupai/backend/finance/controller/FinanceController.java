package io.poupai.backend.finance.controller;

import io.poupai.backend.finance.dto.FinanceDtos;
import io.poupai.backend.finance.service.FinanceService;
import io.poupai.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finances")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    // Resumo dos últimos N meses (endpoint original, agora com mais dados)
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FinanceDtos.FinanceSummaryResponse>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "6") int months
    ) {
        FinanceDtos.FinanceSummaryResponse summary =
                financeService.getSummary(userDetails.getUsername(), months);
        return ResponseEntity.ok(ApiResponse.success("Resumo financeiro recuperado", summary));
    }

    // Resumo de um mês/ano específico
    @GetMapping("/summary/period")
    public ResponseEntity<ApiResponse<FinanceDtos.FinanceSummaryResponse>> getSummaryByPeriod(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int month,
            @RequestParam int year
    ) {
        FinanceDtos.FinanceSummaryResponse summary =
                financeService.getSummaryByPeriod(userDetails.getUsername(), month, year);
        return ResponseEntity.ok(ApiResponse.success("Resumo do período recuperado", summary));
    }
}