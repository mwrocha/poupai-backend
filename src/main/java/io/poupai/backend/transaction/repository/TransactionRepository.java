package io.poupai.backend.transaction.repository;

import io.poupai.backend.transaction.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByUserIdOrderByDateDesc(String userId);

    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
