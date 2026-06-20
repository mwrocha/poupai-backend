package io.poupai.backend.gamification.service;

import io.poupai.backend.gamification.model.AwardedAchievement;
import io.poupai.backend.gamification.model.Gamification;
import io.poupai.backend.gamification.repository.AwardedAchievementRepository;
import io.poupai.backend.gamification.repository.GamificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private GamificationRepository gamificationRepository;

    @Mock
    private AwardedAchievementRepository awardedRepository;

    @InjectMocks
    private GamificationService service;

    private static final String USER = "user-1";
    private static final String GOAL = "goal-1";

    @Test
    void onGoalCompleted_creditsOnlyOncePerGoal_evenWhenReCompleted() {
        Gamification g = Gamification.builder().userId(USER).build();
        when(gamificationRepository.findByUserId(USER)).thenReturn(Optional.of(g));
        // 1ª chamada: não creditado ainda; 2ª chamada (re-conclusão): já creditado
        when(awardedRepository.existsByUserIdAndAchievementKeyAndEntityId(
                USER, GamificationService.GOAL_COMPLETED, GOAL))
                .thenReturn(false, true);

        service.onGoalCompleted(USER, GOAL); // atinge
        service.onGoalCompleted(USER, GOAL); // des-atinge e re-atinge → não pode recreditar

        assertEquals(1, g.getCompletedGoals(), "completedGoals não deve inflar na re-conclusão");
        assertEquals(50, g.getTotalPoints(), "pontos de conclusão devem ser creditados uma única vez");
        verify(awardedRepository, times(1)).insert(any(AwardedAchievement.class));
        verify(gamificationRepository, times(1)).save(g);
    }

    @Test
    void recomputeCompletedGoals_correctsInflatedStateIdempotently() {
        // Estado inflado: 3 conclusões contabilizadas, das quais só 1 é real.
        // pontos = 30 (outras fontes) + 3 * 50 (conclusões) = 180
        Gamification g = Gamification.builder()
                .userId(USER)
                .completedGoals(3)
                .totalPoints(180)
                .build();
        when(gamificationRepository.findByUserId(USER)).thenReturn(Optional.of(g));

        service.recomputeCompletedGoals(USER, 1);
        assertEquals(1, g.getCompletedGoals());
        assertEquals(80, g.getTotalPoints(), "180 - 3*50 + 1*50 = 80");

        // Rodar de novo não deve mudar nada (idempotente)
        service.recomputeCompletedGoals(USER, 1);
        assertEquals(1, g.getCompletedGoals());
        assertEquals(80, g.getTotalPoints());
    }
}
