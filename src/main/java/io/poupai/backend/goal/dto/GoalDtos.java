package io.poupai.backend.goal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GoalDtos {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Título é obrigatório")
        private String title;

        @NotNull(message = "Valor alvo é obrigatório")
        @Positive(message = "Valor alvo deve ser maior que zero")
        private Double targetValue;

        @PositiveOrZero
        private Double currentValue = 0.0;

        private String deadline;
        private String icon;
        private String color;
    }

    @Data
    public static class UpdateProgressRequest {
        @NotNull(message = "Valor atual é obrigatório")
        @PositiveOrZero
        private Double currentValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalResponse {
        private String id;
        private String title;
        private Double targetValue;
        private Double currentValue;
        private String deadline;
        private String icon;
        private String color;
    }
}
