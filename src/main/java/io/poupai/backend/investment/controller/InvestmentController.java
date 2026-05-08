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

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvestmentDtos.InvestmentResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<InvestmentDtos.InvestmentResponse> investments =
                investmentService.getAll(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Investimentos recuperados", investments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvestmentDtos.InvestmentResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InvestmentDtos.CreateRequest request
    ) {
        InvestmentDtos.InvestmentResponse response =
                investmentService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Investimento criado", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestmentDtos.InvestmentResponse>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody InvestmentDtos.UpdateRequest request
    ) {
        InvestmentDtos.InvestmentResponse response =
                investmentService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Investimento atualizado", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        investmentService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Investimento deletado", null));
    }
}
