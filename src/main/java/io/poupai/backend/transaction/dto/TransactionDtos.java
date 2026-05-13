package io.poupai.backend.transaction.dto;

import io.poupai.backend.transaction.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransactionDtos {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Título é obrigatório")
        private String title;

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        private Double amount;

        @NotNull(message = "Tipo é obrigatório")
        private Transaction.TransactionType type;

        @NotBlank(message = "Categoria é obrigatória")
        private String category;

        @NotBlank(message = "Data é obrigatória")
        private String date;

        private String tagId;
    }

    // ─── Edição — todos os campos são opcionais ───
    @Data
    public static class UpdateRequest {
        private String title;
        private Double amount;
        private Transaction.TransactionType type;
        private String category;
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionResponse {
        private String id;
        private String title;
        private Double amount;
        private String type;
        private String category;
        private String date;
        private String tagId;
    }
}