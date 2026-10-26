package za.co.capitec.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaFraudCheckRepository extends JpaRepository<FraudCheckEntity, UUID> {
}