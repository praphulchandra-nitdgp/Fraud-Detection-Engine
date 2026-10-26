package za.co.capitec.usecases;

import za.co.capitec.domain.model.FraudAlert;
import za.co.capitec.ports.FraudAlertRepositoryPort;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record GetFraudAlertsUseCase(FraudAlertRepositoryPort fraudAlertRepositoryPort) {

    public GetFraudAlertsUseCase(FraudAlertRepositoryPort fraudAlertRepositoryPort) {
        this.fraudAlertRepositoryPort = Objects.requireNonNull(
                fraudAlertRepositoryPort,
                "fraudAlertRepositoryPort must not be null"
        );
    }

    public List<FraudAlert> getAll() {
        return fraudAlertRepositoryPort.findAll();
    }

    public List<FraudAlert> getByTransactionId(UUID transactionId) {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        return fraudAlertRepositoryPort.findByTransactionId(transactionId);
    }
}
