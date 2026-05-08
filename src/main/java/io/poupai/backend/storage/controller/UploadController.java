package io.poupai.backend.storage.controller;

import io.poupai.backend.shared.response.ApiResponse;
import io.poupai.backend.user.model.User;
import io.poupai.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.poupai.backend.storage.service.StorageService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storageService;
    private final UserRepository userRepository;

    @PostMapping("/profile-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Deleta foto anterior se existir
        if (user.getProfileImageUrl() != null) {
            storageService.deleteFile(user.getProfileImageUrl());
        }

        String imageUrl = storageService.uploadProfileImage(file, user.getId());

        // Atualiza o campo no MongoDB
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                "Foto de perfil atualizada",
                Map.of("url", imageUrl)
        ));
    }

    // Endpoint sem auth — usado durante o cadastro antes do token existir
    @PostMapping("/profile-image/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImageRegister(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tempId") String tempId
    ) throws IOException {
        String imageUrl = storageService.uploadProfileImage(file, "temp_" + tempId);
        return ResponseEntity.ok(ApiResponse.success(
                "Imagem enviada",
                Map.of("url", imageUrl)
        ));
    }
}