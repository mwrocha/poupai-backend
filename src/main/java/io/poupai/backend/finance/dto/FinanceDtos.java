package io.poupai.backend.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class FinanceDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinanceSummaryResponse {
        private List<Double> incomeHistory;
        private List<Double> expenseHistory;
        private List<Double> profitHistory;
    }
}
