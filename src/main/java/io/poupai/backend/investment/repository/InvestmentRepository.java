package io.poupai.backend.investment.repository;

import io.poupai.backend.investment.model.Investment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRepository extends MongoRepository<Investment, String> {

    List<Investment> findByUserId(String userId);

    List<Investment> findByUserIdAndType(String userId, Investment.InvestmentType type);
}
