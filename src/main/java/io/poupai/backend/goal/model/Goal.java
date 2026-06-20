package io.poupai.backend.goal.model;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "goals")
public class Goal {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private Double targetValue;
    private Double currentValue;
    private String deadline;
    private String icon;
    private String color;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Definição única de "meta concluída", alinhada ao frontend
     * (targetValue > 0 && currentValue >= targetValue).
     */
    public boolean isCompleted() {
        return targetValue != null && targetValue > 0
                && currentValue != null && currentValue >= targetValue;
    }
}
