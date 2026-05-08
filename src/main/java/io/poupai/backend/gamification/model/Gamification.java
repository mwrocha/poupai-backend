package io.poupai.backend.gamification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gamification")
public class Gamification {

    @Id
    private String id;

    private String userId;

    // ─── Pontos ───
    @Builder.Default
    private int totalPoints = 0;

    // ─── Streak ───
    @Builder.Default
    private int currentStreak = 0;

    @Builder.Default
    private int longestStreak = 0;

    private LocalDate lastActivityDate;

    // ─── Badges desbloqueados ───
    @Builder.Default
    private List<String> unlockedBadges = new ArrayList<>();

    // ─── Contadores para checar badges ───
    @Builder.Default
    private int totalTransactions = 0;

    @Builder.Default
    private int totalInvestments = 0;

    @Builder.Default
    private int totalGoals = 0;

    @Builder.Default
    private int completedGoals = 0;
}