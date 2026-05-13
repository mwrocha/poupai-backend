package io.poupai.backend.investment.repository;

import io.poupai.backend.investment.model.Dividend;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividendRepository extends MongoRepository<Dividend, String> {
    List<Dividend> findByUserIdOrderByDateDesc(String userId);
    List<Dividend> findByUserIdAndInvestmentIdOrderByDateDesc(String userId, String investmentId);
    List<Dividend> findByUserIdAndDateBetweenOrderByDateDesc(String userId, LocalDate start, LocalDate end);
}