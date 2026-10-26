package za.co.capitec.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    @Query("""
            select count(t)
            from TransactionEntity t
            where t.accountId = :accountId
              and t.timestamp >= :fromTimestamp
              and t.timestamp <= :toTimestamp
            """)
    long countTransactionsWithinWindow(
            @Param("accountId") String accountId,
            @Param("fromTimestamp") Instant fromTimestamp,
            @Param("toTimestamp") Instant toTimestamp
    );
}
