package io.poupai.backend.investment.dto;

import io.poupai.backend.investment.model.Investment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class InvestmentDtos {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Nome é obrigatório")
        private String name;

        @NotNull(message = "Tipo é obrigatório")
        private Investment.InvestmentType type;

        @NotNull(message = "Valor atual é obrigatório")
        @PositiveOrZero
        private Double currentValue;

        @NotNull(message = "Valor investido é obrigatório")
        @PositiveOrZero
        private Double investedValue;
    }

    @Data
    public static class UpdateRequest {
        private Double currentValue;
        private Double investedValue;
    }

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
    }
}
