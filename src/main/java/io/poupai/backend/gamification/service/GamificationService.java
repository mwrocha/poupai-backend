package io.poupai.backend.gamification.service;

import io.poupai.backend.gamification.dto.GamificationDtos;
import io.poupai.backend.gamification.model.Gamification;
import io.poupai.backend.gamification.repository.GamificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final GamificationRepository gamificationRepository;

    // ─── Definição de todos os badges ───
    private static final List<BadgeDefinition> ALL_BADGES = Arrays.asList(
            new BadgeDefinition("first_transaction", "Primeira Transação", "Registrou sua primeira transação", "🏁"),
            new BadgeDefinition("on_fire", "Em Chamas", "7 dias consecutivos de atividade", "🔥"),
            new BadgeDefinition("first_goal", "Focado", "Criou sua primeira meta", "🎯"),
            new BadgeDefinition("goal_completed", "Realizador", "Concluiu uma meta financeira", "✅"),
            new BadgeDefinition("first_investment", "Investidor", "Cadastrou seu primeiro investimento", "📈"),
            new BadgeDefinition("saver", "Poupador", "Acumulou 100 pontos", "💰")
    );

    // ─── Busca ou cria o registro do usuário ───
    private Gamification getOrCreate(String userId) {
        return gamificationRepository.findByUserId(userId)
                .orElseGet(() -> gamificationRepository.save(
                        Gamification.builder().userId(userId).build()
                ));
    }

    // ─── Evento: transação criada ───
    public void onTransactionCreated(String userId) {
        Gamification g = getOrCreate(userId);

        g.setTotalTransactions(g.getTotalTransactions() + 1);
        g.setTotalPoints(g.getTotalPoints() + 10);

        updateStreak(g);
        checkBadges(g);

        gamificationRepository.save(g);
    }

    // ─── Evento: investimento criado ───
    public void onInvestmentCreated(String userId) {
        Gamification g = getOrCreate(userId);

        g.setTotalInvestments(g.getTotalInvestments() + 1);
        g.setTotalPoints(g.getTotalPoints() + 20);

        updateStreak(g);
        checkBadges(g);

        gamificationRepository.save(g);
    }

    // ─── Evento: meta criada ───
    public void onGoalCreated(String userId) {
        Gamification g = getOrCreate(userId);

        g.setTotalGoals(g.getTotalGoals() + 1);
        g.setTotalPoints(g.getTotalPoints() + 30);

        updateStreak(g);
        checkBadges(g);

        gamificationRepository.save(g);
    }

    // ─── Evento: meta concluída ───
    public void onGoalCompleted(String userId) {
        Gamification g = getOrCreate(userId);

        g.setCompletedGoals(g.getCompletedGoals() + 1);
        g.setTotalPoints(g.getTotalPoints() + 50);

        checkBadges(g);

        gamificationRepository.save(g);
    }

    // ─── Atualiza streak ───
    private void updateStreak(Gamification g) {
        LocalDate today = LocalDate.now();
        LocalDate last = g.getLastActivityDate();

        if (last == null) {
            g.setCurrentStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(last, today);
            if (daysBetween == 0) {
                // Mesma data — não altera streak
            } else if (daysBetween == 1) {
                // Dia seguinte — incrementa
                g.setCurrentStreak(g.getCurrentStreak() + 1);
            } else {
                // Sequência quebrada
                g.setCurrentStreak(1);
            }
        }

        if (g.getCurrentStreak() > g.getLongestStreak()) {
            g.setLongestStreak(g.getCurrentStreak());
        }

        g.setLastActivityDate(today);
    }

    // ─── Verifica e desbloqueia badges ───
    private void checkBadges(Gamification g) {
        unlock(g, "first_transaction", g.getTotalTransactions() >= 1);
        unlock(g, "on_fire", g.getCurrentStreak() >= 7);
        unlock(g, "first_goal", g.getTotalGoals() >= 1);
        unlock(g, "goal_completed", g.getCompletedGoals() >= 1);
        unlock(g, "first_investment", g.getTotalInvestments() >= 1);
        unlock(g, "saver", g.getTotalPoints() >= 100);
    }

    private void unlock(Gamification g, String badgeId, boolean condition) {
        if (condition && !g.getUnlockedBadges().contains(badgeId)) {
            g.getUnlockedBadges().add(badgeId);
        }
    }

    // ─── GET — retorna estado completo ───
    public GamificationDtos.GamificationResponse getStatus(String userId) {
        Gamification g = getOrCreate(userId);

        List<GamificationDtos.BadgeResponse> badges = ALL_BADGES.stream()
                .map(def -> GamificationDtos.BadgeResponse.builder()
                        .id(def.id())
                        .title(def.title())
                        .description(def.description())
                        .emoji(def.emoji())
                        .unlocked(g.getUnlockedBadges().contains(def.id()))
                        .build())
                .toList();

        return GamificationDtos.GamificationResponse.builder()
                .totalPoints(g.getTotalPoints())
                .currentStreak(g.getCurrentStreak())
                .longestStreak(g.getLongestStreak())
                .badges(badges)
                .totalTransactions(g.getTotalTransactions())
                .totalInvestments(g.getTotalInvestments())
                .totalGoals(g.getTotalGoals())
                .completedGoals(g.getCompletedGoals())
                .build();
    }

    // ─── Record interno para definição de badge ───
    private record BadgeDefinition(String id, String title, String description, String emoji) {
    }
}