package io.poupai.backend.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GamificationDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GamificationResponse {
        private int totalPoints;
        private int currentStreak;
        private int longestStreak;
        private List<BadgeResponse> badges;
        private int totalTransactions;
        private int totalInvestments;
        private int totalGoals;
        private int completedGoals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BadgeResponse {
        private String id;
        private String title;
        private String description;
        private String emoji;
        private boolean unlocked;
    }
}