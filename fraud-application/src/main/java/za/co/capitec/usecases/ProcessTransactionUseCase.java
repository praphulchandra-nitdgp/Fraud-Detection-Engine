package za.co.capitec.usecases;

import za.co.capitec.domain.model.FraudAlert;
import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.domain.model.RuleResult;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.domain.services.FraudDetectionService;
import za.co.capitec.ports.FraudAlertRepositoryPort;
import za.co.capitec.ports.FraudCheckRepositoryPort;
import za.co.capitec.ports.TransactionRepositoryPort;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ProcessTransactionUseCase(TransactionRepositoryPort transactionRepositoryPort,
                                        FraudCheckRepositoryPort fraudCheckRepositoryPort,
                                        FraudAlertRepositoryPort fraudAlertRepositoryPort,
                                        FraudDetectionService fraudDetectionService,
                                        Tracer tracer) {

    public ProcessTransactionUseCase(
            TransactionRepositoryPort transactionRepositoryPort,
            FraudCheckRepositoryPort fraudCheckRepositoryPort,
            FraudAlertRepositoryPort fraudAlertRepositoryPort,
            FraudDetectionService fraudDetectionService,
            Tracer tracer
    ) {
        this.transactionRepositoryPort = Objects.requireNonNull(transactionRepositoryPort, "transactionRepositoryPort must not be null");
        this.fraudCheckRepositoryPort = Objects.requireNonNull(fraudCheckRepositoryPort, "fraudCheckRepositoryPort must not be null");
        this.fraudAlertRepositoryPort = Objects.requireNonNull(fraudAlertRepositoryPort, "fraudAlertRepositoryPort must not be null");
        this.fraudDetectionService = Objects.requireNonNull(fraudDetectionService, "fraudDetectionService must not be null");
        this.tracer = Objects.requireNonNull(tracer, "tracer must not be null");
    }

    public FraudCheck process(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");

        Span span = tracer.spanBuilder("fraud.processTransaction").startSpan();

        try {

            span.setAttribute("fraud.accountId", transaction.accountId());
            span.setAttribute("fraud.amount", transaction.amount().doubleValue());
            transactionRepositoryPort.save(transaction);

            FraudCheck fraudCheck = fraudDetectionService.evaluate(transaction);
            fraudCheckRepositoryPort.save(fraudCheck);

            if (fraudCheck.isFraudSuspected()) {
                fraudAlertRepositoryPort.save(toFraudAlert(fraudCheck));
            }

            span.setAttribute("fraud.riskScore", fraudCheck.riskScore());
            span.setAttribute("fraud.riskLevel", fraudCheck.riskLevel().name());


            return fraudCheck;
        } finally {
            span.end();
        }
    }

    private FraudAlert toFraudAlert(FraudCheck fraudCheck) {
        List<String> triggeredRules = fraudCheck.ruleResults().stream()
                .filter(RuleResult::triggered)
                .map(RuleResult::ruleName)
                .toList();

        return new FraudAlert(
                UUID.randomUUID(),
                fraudCheck.transactionId(),
                fraudCheck.riskScore(),
                fraudCheck.riskLevel(),
                triggeredRules,
                Instant.now()
        );
    }
}
