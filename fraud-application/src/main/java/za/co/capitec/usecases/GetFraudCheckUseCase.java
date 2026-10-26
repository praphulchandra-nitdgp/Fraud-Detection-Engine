package za.co.capitec.usecases;

import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.ports.FraudCheckRepositoryPort;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GetFraudCheckUseCase(FraudCheckRepositoryPort fraudCheckRepositoryPort) {

    public GetFraudCheckUseCase(FraudCheckRepositoryPort fraudCheckRepositoryPort) {
        this.fraudCheckRepositoryPort = Objects.requireNonNull(
                fraudCheckRepositoryPort,
                "fraudCheckRepositoryPort must not be null"
        );
    }

    public Optional<FraudCheck> getByTransactionId(UUID transactionId) {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        return fraudCheckRepositoryPort.findByTransactionId(transactionId);
    }
}
