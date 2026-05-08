package io.poupai.backend.gamification.repository;

import io.poupai.backend.gamification.model.Gamification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamificationRepository extends MongoRepository<Gamification, String> {
    Optional<Gamification> findByUserId(String userId);
}