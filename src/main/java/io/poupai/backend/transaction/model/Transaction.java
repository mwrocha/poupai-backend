package io.poupai.backend.transaction.model;

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
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private Double amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String tagId;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum TransactionType {
        INCOME, EXPENSE
    }
}
