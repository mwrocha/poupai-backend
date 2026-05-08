package io.poupai.backend.tag.controller;

import io.poupai.backend.shared.response.ApiResponse;
import io.poupai.backend.tag.dto.TagDtos;
import io.poupai.backend.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * GET /tags/summary?month=5&year=2026
     * Retorna categorias agregadas por total gasto.
     * month e year são opcionais — se omitidos retorna todos os dados.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TagDtos.TagsSummaryResponse>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        String userId = userDetails.getUsername();
        TagDtos.TagsSummaryResponse summary = tagService.getTagsSummary(userId, month, year);
        return ResponseEntity.ok(ApiResponse.success("Tags carregadas com sucesso", summary));
    }
}