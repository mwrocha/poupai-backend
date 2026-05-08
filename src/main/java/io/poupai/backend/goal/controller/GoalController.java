package io.poupai.backend.goal.controller;

import io.poupai.backend.goal.dto.GoalDtos;
import io.poupai.backend.goal.service.GoalService;
import io.poupai.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalDtos.GoalResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<GoalDtos.GoalResponse> goals = goalService.getAll(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Metas recuperadas", goals));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalDtos.GoalResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GoalDtos.CreateRequest request
    ) {
        GoalDtos.GoalResponse response = goalService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Meta criada", response));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalDtos.GoalResponse>> updateProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody GoalDtos.UpdateProgressRequest request
    ) {
        GoalDtos.GoalResponse response = goalService.updateProgress(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Progresso atualizado", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        goalService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Meta deletada", null));
    }
}
