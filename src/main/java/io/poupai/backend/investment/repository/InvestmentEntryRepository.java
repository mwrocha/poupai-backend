package io.poupai.backend.investment.repository;

import io.poupai.backend.investment.model.InvestmentEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvestmentEntryRepository extends MongoRepository<InvestmentEntry, String> {
    List<InvestmentEntry> findByUserIdOrderByDateDesc(String userId);
    List<InvestmentEntry> findByUserIdAndInvestmentIdOrderByDateDesc(String userId, String investmentId);
    List<InvestmentEntry> findByUserIdAndDateBetweenOrderByDateDesc(String userId, LocalDate start, LocalDate end);
    List<InvestmentEntry> findByUserIdAndInvestmentIdAndDateBetweenOrderByDateDesc(String userId, String investmentId, LocalDate start, LocalDate end);
}