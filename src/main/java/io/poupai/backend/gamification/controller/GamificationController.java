package io.poupai.backend.gamification.controller;

import io.poupai.backend.gamification.dto.GamificationDtos;
import io.poupai.backend.gamification.service.GamificationService;
import io.poupai.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    /**
     * GET /gamification/status
     * Retorna pontos, streak e badges do usuário logado.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<GamificationDtos.GamificationResponse>> getStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        GamificationDtos.GamificationResponse response =
                gamificationService.getStatus(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Status de gamificação", response));
    }
}