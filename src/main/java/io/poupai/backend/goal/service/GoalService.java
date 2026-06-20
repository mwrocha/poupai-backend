package io.poupai.backend.goal.service;

import io.poupai.backend.gamification.service.GamificationService;
import io.poupai.backend.goal.dto.GoalDtos;
import io.poupai.backend.goal.model.Goal;
import io.poupai.backend.goal.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GamificationService gamificationService;

    public List<GoalDtos.GoalResponse> getAll(String userId) {
        return goalRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public GoalDtos.GoalResponse create(String userId, GoalDtos.CreateRequest request) {
        Goal goal = Goal.builder()
                .userId(userId)
                .title(request.getTitle())
                .targetValue(request.getTargetValue())
                .currentValue(request.getCurrentValue() != null ? request.getCurrentValue() : 0.0)
                .deadline(request.getDeadline())
                .icon(request.getIcon() != null ? request.getIcon() : "🎯")
                .color(request.getColor() != null ? request.getColor() : "#503173")
                .build();

        GoalDtos.GoalResponse response = toResponse(goalRepository.save(goal));

        // ─── Gamificação ───
        gamificationService.onGoalCreated(userId);

        return response;
    }

    public GoalDtos.GoalResponse updateProgress(String userId, String goalId, GoalDtos.UpdateProgressRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));
        if (!goal.getUserId().equals(userId))
            throw new RuntimeException("Sem permissão para editar esta meta");

        boolean wasCompleted = goal.isCompleted();
        goal.setCurrentValue(request.getCurrentValue());
        GoalDtos.GoalResponse response = toResponse(goalRepository.save(goal));

        // ─── Gamificação — transição "não-concluída → concluída".
        // A idempotência real (não recreditar a mesma meta) vive no GamificationService. ───
        if (!wasCompleted && goal.isCompleted()) {
            gamificationService.onGoalCompleted(userId, goal.getId());
        }

        return response;
    }

    public void delete(String userId, String goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));
        if (!goal.getUserId().equals(userId))
            throw new RuntimeException("Sem permissão para deletar esta meta");
        goalRepository.delete(goal);
    }

    private GoalDtos.GoalResponse toResponse(Goal g) {
        return GoalDtos.GoalResponse.builder()
                .id(g.getId()).title(g.getTitle())
                .targetValue(g.getTargetValue()).currentValue(g.getCurrentValue())
                .deadline(g.getDeadline()).icon(g.getIcon()).color(g.getColor())
                .build();
    }
}