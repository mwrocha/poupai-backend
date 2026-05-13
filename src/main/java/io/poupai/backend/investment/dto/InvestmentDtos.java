package io.poupai.backend.investment.dto;

import io.poupai.backend.investment.model.Dividend;
import io.poupai.backend.investment.model.Investment;
import io.poupai.backend.investment.model.InvestmentEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class InvestmentDtos {

    // ─── Criar ativo ───
    @Data
    public static class CreateRequest {
        @NotBlank(message = "Nome é obrigatório")
        private String name;

        @NotNull(message = "Tipo é obrigatório")
        private Investment.InvestmentType type;

        @NotNull @PositiveOrZero
        private Double currentValue;

        @NotNull @PositiveOrZero
        private Double investedValue;

        @PositiveOrZero
        private Double shares;

        @PositiveOrZero
        private Double allocationTarget;
    }

    // ─── Atualizar ativo (só metadados — aportes vão para Entry) ───
    @Data
    public static class UpdateRequest {
        private String name;
        private Double currentValue;     // Atualização manual de valor
        private Double allocationTarget;
    }

    // ─── Response do ativo ───
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentResponse {
        private String id;
        private String name;
        private String type;
        private Double currentValue;
        private Double investedValue;
        private Double profitability;
        private Double shares;
        private Double averagePrice;
        private Double allocationTarget;
        private List<SnapshotResponse> history;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnapshotResponse {
        private String date;
        private Double value;
        private Double invested;
        private Double profitability;
    }

    // ─── Lançamento no livro contábil ───
    @Data
    public static class CreateEntryRequest {
        @NotBlank
        private String investmentId;

        @NotNull
        private InvestmentEntry.EntryType type;

        // Para APORTE e RESGATE
        private Double shares;
        private Double sharePrice;

        // Para ATUALIZACAO_VALOR
        private Double newCurrentValue;

        private String notes;

        @NotBlank
        private String date; // "yyyy-MM-dd"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntryResponse {
        private String id;
        private String investmentId;
        private String investmentName;
        private String type;
        private Double shares;
        private Double sharePrice;
        private Double totalValue;
        private Double previousShares;
        private Double previousAveragePrice;
        private Double newAveragePrice;
        private Double newTotalShares;
        private String notes;
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntrySummaryResponse {
        private List<EntryResponse> entries;
        private Double totalAported;
        private Double totalRescued;
        private Long totalEntries;
    }

    // ─── Dividendos ───
    @Data
    public static class CreateDividendRequest {
        @NotBlank
        private String investmentId;

        @NotNull @PositiveOrZero
        private Double amount;

        @NotNull
        private Dividend.DividendType type;

        @NotBlank
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendResponse {
        private String id;
        private String investmentId;
        private String investmentName;
        private Double amount;
        private Double yieldPercent;
        private String date;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendSummaryResponse {
        private List<DividendResponse> dividends;
        private Double totalReceived;
        private Double totalReceivedThisYear;
        private Double totalReceivedThisMonth;
        private Double projectedAnnual;
    }

    // ─── Rebalanceamento ───
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebalanceResponse {
        private List<RebalanceItem> items;
        private Double totalCurrentValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebalanceItem {
        private String investmentId;
        private String name;
        private String type;
        private Double currentValue;
        private Double currentPercent;
        private Double targetPercent;
        private Double difference;
        private String action;
        private Double amountToAdjust;
    }

    // ─── Benchmark ───
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenchmarkResponse {
        private Double cdiRateYear;
        private Double cdiRateMonth;
        private Double portfolioReturn;
        private Double vsCdi;
        private String lastUpdated;
    }
}