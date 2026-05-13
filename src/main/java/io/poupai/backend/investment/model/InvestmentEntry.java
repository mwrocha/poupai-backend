package io.poupai.backend.investment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "investment_entries")
public class InvestmentEntry {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String investmentId;

    private String investmentName; // normalizado

    private EntryType type;

    private Double shares;                // Cotas desta movimentação
    private Double sharePrice;            // Preço por cota
    private Double totalValue;            // Valor total (shares * sharePrice)

    private Double previousShares;        // Cotas antes da movimentação
    private Double previousAveragePrice;  // PM antes da movimentação
    private Double newAveragePrice;       // PM após (calculado)
    private Double newTotalShares;        // Total de cotas após

    private String notes;                 // Observação opcional

    private LocalDate date;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum EntryType {
        APORTE,
        RESGATE,
        ATUALIZACAO_VALOR,
    }
}