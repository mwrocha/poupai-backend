package io.poupai.backend.gamification.migration;

import io.poupai.backend.gamification.model.AwardedAchievement;
import io.poupai.backend.gamification.model.Gamification;
import io.poupai.backend.gamification.repository.AwardedAchievementRepository;
import io.poupai.backend.gamification.repository.GamificationRepository;
import io.poupai.backend.gamification.service.GamificationService;
import io.poupai.backend.goal.model.Goal;
import io.poupai.backend.goal.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Migração de histórico para o bug de idempotência do completedGoals.
 *
 * Para cada usuário:
 *  1. Faz backfill de awarded_achievements para cada meta concluída.
 *  2. Recalcula completedGoals a partir da fonte de verdade (metas concluídas)
 *     e corrige os pontos/badges via {@link GamificationService#recomputeCompletedGoals}.
 *
 * Roda uma única vez (marcador em applied_migrations) e é idempotente —
 * seguro re-executar caso o marcador seja removido.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GamificationRecomputeMigration implements ApplicationRunner {

    static final String MIGRATION_ID = "2026-05-recompute-completed-goals";

    private final MongoTemplate mongoTemplate;
    private final GamificationRepository gamificationRepository;
    private final GoalRepository goalRepository;
    private final AwardedAchievementRepository awardedRepository;
    private final GamificationService gamificationService;

    @Override
    public void run(ApplicationArguments args) {
        // 1. Garante o índice único — a idempotência em runtime depende dele.
        mongoTemplate.indexOps(AwardedAchievement.class).ensureIndex(
                new Index()
                        .on("userId", Sort.Direction.ASC)
                        .on("achievementKey", Sort.Direction.ASC)
                        .on("entityId", Sort.Direction.ASC)
                        .unique()
                        .named("uniq_award"));

        // 2. Executa apenas uma vez.
        if (mongoTemplate.findById(MIGRATION_ID, AppliedMigration.class) != null) {
            return;
        }

        log.info("[migration {}] iniciando recálculo de gamificação", MIGRATION_ID);
        int usersRecomputed = 0;

        for (Gamification g : gamificationRepository.findAll()) {
            String userId = g.getUserId();
            List<Goal> goals = goalRepository.findByUserId(userId);

            // Backfill dos registros de conquista para metas concluídas.
            for (Goal goal : goals) {
                if (goal.isCompleted()
                        && !awardedRepository.existsByUserIdAndAchievementKeyAndEntityId(
                                userId, GamificationService.GOAL_COMPLETED, goal.getId())) {
                    try {
                        awardedRepository.insert(AwardedAchievement.builder()
                                .userId(userId)
                                .achievementKey(GamificationService.GOAL_COMPLETED)
                                .entityId(goal.getId())
                                .build());
                    } catch (DuplicateKeyException ignored) {
                        // já existe — ok
                    }
                }
            }

            int actualCompleted = (int) goals.stream().filter(Goal::isCompleted).count();
            gamificationService.recomputeCompletedGoals(userId, actualCompleted);
            usersRecomputed++;
        }

        mongoTemplate.save(new AppliedMigration(MIGRATION_ID, LocalDateTime.now()));
        log.info("[migration {}] concluída — {} usuários recalculados", MIGRATION_ID, usersRecomputed);
    }
}
