package io.poupai.backend.auth.controller;

import io.poupai.backend.auth.dto.AuthDtos;
import io.poupai.backend.auth.service.AuthService;
import io.poupai.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request
    ) {
        AuthDtos.AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Cadastro realizado com sucesso", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(
            @Valid @RequestBody AuthDtos.LoginRequest request
    ) {
        AuthDtos.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login realizado com sucesso", response));
    }
}
