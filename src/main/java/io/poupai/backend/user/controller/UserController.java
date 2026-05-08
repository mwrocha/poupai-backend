package io.poupai.backend.user.controller;

import io.poupai.backend.shared.response.ApiResponse;
import io.poupai.backend.user.dto.UserDtos;
import io.poupai.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDtos.UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserDtos.UserResponse response = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Perfil recuperado", response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDtos.UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDtos.UpdateProfileRequest request
    ) {
        UserDtos.UserResponse response =
                userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Perfil atualizado", response));
    }

    @PutMapping("/email")
    public ResponseEntity<ApiResponse<UserDtos.UserResponse>> updateEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDtos.UpdateEmailRequest request
    ) {
        UserDtos.UserResponse response =
                userService.updateEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("E-mail atualizado. Faça login novamente.", response));
    }
}