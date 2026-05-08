package io.poupai.backend.goal.repository;

import io.poupai.backend.goal.model.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends MongoRepository<Goal, String> {

    List<Goal> findByUserId(String userId);
}
