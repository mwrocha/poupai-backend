package io.poupai.backend.gamification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Registro de uma conquista já creditada para um usuário.
 * Garante idempotência: a mesma conquista (userId + achievementKey + entityId)
 * nunca é creditada duas vezes. O índice único composto é a barreira durável.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "awarded_achievements")
@CompoundIndex(name = "uniq_award", def = "{'userId': 1, 'achievementKey': 1, 'entityId': 1}", unique = true)
public class AwardedAchievement {

    @Id
    private String id;

    private String userId;

    private String achievementKey;   // ex: "GOAL_COMPLETED"

    private String entityId;         // ex: goalId

    @CreatedDate
    private LocalDateTime awardedAt;
}
