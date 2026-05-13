package io.poupai.backend.investment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "investments")
public class Investment {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String name;
    private InvestmentType type;
    private Double currentValue;
    private Double investedValue;
    private Double profitability;

    // ─── Preço médio ───
    @Builder.Default
    private Double shares = 0.0;           // Quantidade de cotas/ações

    @Builder.Default
    private Double averagePrice = 0.0;     // Preço médio calculado

    // ─── Rebalanceamento ───
    @Builder.Default
    private Double allocationTarget = 0.0; // % alvo definido pelo usuário (0-100)

    // ─── Histórico de rentabilidade ───
    @Builder.Default
    private List<ProfitabilitySnapshot> history = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum InvestmentType {
        RENDA_VARIAVEL,
        RENDA_FIXA,
        CRIPTOMOEDAS
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitabilitySnapshot {
        private String date;        // "yyyy-MM-dd"
        private Double value;       // Valor atual na data
        private Double invested;    // Total investido na data
        private Double profitability; // % na data
    }
}