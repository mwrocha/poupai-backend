package io.poupai.backend.gamification.repository;

import io.poupai.backend.gamification.model.AwardedAchievement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AwardedAchievementRepository extends MongoRepository<AwardedAchievement, String> {

    boolean existsByUserIdAndAchievementKeyAndEntityId(String userId, String achievementKey, String entityId);
}
