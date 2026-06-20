package io.poupai.backend.goal.service;

import io.poupai.backend.gamification.service.GamificationService;
import io.poupai.backend.goal.dto.GoalDtos;
import io.poupai.backend.goal.model.Goal;
import io.poupai.backend.goal.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GamificationService gamificationService;

    @InjectMocks
    private GoalService service;

    private static final String USER = "user-1";
    private static final String GOAL = "goal-1";

    private Goal goal;

    @BeforeEach
    void setUp() {
        goal = Goal.builder()
                .id(GOAL).userId(USER).title("Reserva")
                .targetValue(100.0).currentValue(0.0)
                .build();
        lenient().when(goalRepository.findById(GOAL)).thenReturn(Optional.of(goal));
        lenient().when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void reachingTarget_firesGoalCompletedWithGoalId() {
        service.updateProgress(USER, GOAL, progress(100.0));
        verify(gamificationService).onGoalCompleted(USER, GOAL);
    }

    @Test
    void stayingBelowTarget_doesNotFire() {
        service.updateProgress(USER, GOAL, progress(50.0));
        verify(gamificationService, never()).onGoalCompleted(anyString(), anyString());
    }

    @Test
    void alreadyCompleted_adjustedDown_doesNotFire() {
        goal.setCurrentValue(100.0); // já estava concluída
        service.updateProgress(USER, GOAL, progress(50.0));
        verify(gamificationService, never()).onGoalCompleted(anyString(), anyString());
    }

    private GoalDtos.UpdateProgressRequest progress(double value) {
        GoalDtos.UpdateProgressRequest request = new GoalDtos.UpdateProgressRequest();
        request.setCurrentValue(value);
        return request;
    }
}
