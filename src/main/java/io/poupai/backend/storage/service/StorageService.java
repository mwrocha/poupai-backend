package io.poupai.backend.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    // URL pública do bucket — gerada pelo Cloudflare R2 Public Development URL
    private static final String PUBLIC_BASE_URL = "https://pub-1f21d751cd214aff8ca7c31e3abf578d.r2.dev";

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/*"
    );

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadProfileImage(MultipartFile file, String userId) throws IOException {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String key = "profiles/" + userId + "/" + UUID.randomUUID() + "." + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        // Retorna a URL pública acessível pelo app
        String publicUrl = PUBLIC_BASE_URL + "/" + key;
        log.info("Upload concluído: {}", publicUrl);
        return publicUrl;
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = fileUrl.replace(PUBLIC_BASE_URL + "/", "");
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("Arquivo deletado: {}", key);
        } catch (Exception e) {
            log.warn("Erro ao deletar arquivo: {}", e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("Arquivo vazio");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new RuntimeException("Formato inválido. Use JPEG, PNG ou WebP");
        if (file.getSize() > MAX_SIZE)
            throw new RuntimeException("Arquivo muito grande. Máximo 5MB");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}