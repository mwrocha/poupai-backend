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
@Document(collection = "dividends")
public class Dividend {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String investmentId;   // Referência ao ativo

    private String investmentName; // Denormalizado para evitar joins

    private Double amount;         // Valor recebido
    private Double yieldPercent;   // Yield % calculado (amount / investedValue * 100)
    private LocalDate date;        // Data do recebimento

    private DividendType type;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum DividendType {
        DIVIDENDO,
        JCP,           // Juros sobre Capital Próprio
        RENDIMENTO,    // FII
        AMORTIZACAO,
        OUTROS
    }
}