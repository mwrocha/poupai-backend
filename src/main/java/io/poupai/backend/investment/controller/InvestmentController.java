package io.poupai.backend.investment.controller;

import io.poupai.backend.investment.dto.InvestmentDtos;
import io.poupai.backend.investment.service.InvestmentService;
import io.poupai.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    // ─── ATIVOS ───

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvestmentDtos.InvestmentResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Investimentos recuperados",
                investmentService.getAll(userDetails.getUsername())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvestmentDtos.InvestmentResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InvestmentDtos.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Investimento criado",
                investmentService.create(userDetails.getUsername(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestmentDtos.InvestmentResponse>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody InvestmentDtos.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Investimento atualizado",
                investmentService.update(userDetails.getUsername(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        investmentService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Investimento deletado", null));
    }

    // ─── LIVRO CONTÁBIL ───

    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<InvestmentDtos.EntrySummaryResponse>> getEntries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String investmentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success("Lançamentos recuperados",
                investmentService.getEntries(userDetails.getUsername(), investmentId, year, month)));
    }

    @PostMapping("/entries")
    public ResponseEntity<ApiResponse<InvestmentDtos.EntryResponse>> addEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InvestmentDtos.CreateEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Lançamento registrado",
                investmentService.addEntry(userDetails.getUsername(), request)));
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String entryId) {
        investmentService.deleteEntry(userDetails.getUsername(), entryId);
        return ResponseEntity.ok(ApiResponse.success("Lançamento excluído", null));
    }

    // ─── DIVIDENDOS ───

    @GetMapping("/dividends")
    public ResponseEntity<ApiResponse<InvestmentDtos.DividendSummaryResponse>> getDividends(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success("Dividendos recuperados",
                investmentService.getDividends(userDetails.getUsername(), year, month)));
    }

    @PostMapping("/dividends")
    public ResponseEntity<ApiResponse<InvestmentDtos.DividendResponse>> addDividend(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InvestmentDtos.CreateDividendRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Dividendo registrado",
                investmentService.addDividend(userDetails.getUsername(), request)));
    }

    @DeleteMapping("/dividends/{dividendId}")
    public ResponseEntity<ApiResponse<Void>> deleteDividend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String dividendId) {
        investmentService.deleteDividend(userDetails.getUsername(), dividendId);
        return ResponseEntity.ok(ApiResponse.success("Dividendo deletado", null));
    }

    // ─── REBALANCEAMENTO ───

    @GetMapping("/rebalance")
    public ResponseEntity<ApiResponse<InvestmentDtos.RebalanceResponse>> getRebalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Rebalanceamento calculado",
                investmentService.getRebalance(userDetails.getUsername())));
    }

    // ─── BENCHMARK ───

    @GetMapping("/benchmark")
    public ResponseEntity<ApiResponse<InvestmentDtos.BenchmarkResponse>> getBenchmark(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Benchmark calculado",
                investmentService.getBenchmark(userDetails.getUsername())));
    }
}