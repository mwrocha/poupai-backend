package io.poupai.backend.gamification.migration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Marcador de migração já aplicada. Garante execução única no startup.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applied_migrations")
public class AppliedMigration {

    @Id
    private String id;

    private LocalDateTime appliedAt;
}
