package za.co.capitec;


import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import za.co.capitec.domain.services.FraudDetectionService;
import za.co.capitec.ports.FraudAlertRepositoryPort;
import za.co.capitec.ports.FraudCheckRepositoryPort;
import za.co.capitec.ports.TransactionRepositoryPort;
import za.co.capitec.usecases.GetFraudAlertsUseCase;
import za.co.capitec.usecases.GetFraudCheckUseCase;
import za.co.capitec.usecases.ProcessTransactionUseCase;

@SpringBootApplication
public class FraudEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudEngineApplication.class, args);
    }

    @Bean
    ProcessTransactionUseCase processTransactionUseCase(
            TransactionRepositoryPort transactionRepositoryPort,
            FraudCheckRepositoryPort fraudCheckRepositoryPort,
            FraudAlertRepositoryPort fraudAlertRepositoryPort,
            FraudDetectionService fraudDetectionService,
            Tracer tracer
    ) {
        return new ProcessTransactionUseCase(
                transactionRepositoryPort,
                fraudCheckRepositoryPort,
                fraudAlertRepositoryPort,
                fraudDetectionService,
                tracer
        );
    }

    @Bean
    GetFraudCheckUseCase getFraudCheckUseCase(FraudCheckRepositoryPort fraudCheckRepositoryPort) {
        return new GetFraudCheckUseCase(fraudCheckRepositoryPort);
    }

    @Bean
    GetFraudAlertsUseCase getFraudAlertsUseCase(FraudAlertRepositoryPort fraudAlertRepositoryPort) {
        return new GetFraudAlertsUseCase(fraudAlertRepositoryPort);
    }
    @Bean
    Tracer tracer() {
        return GlobalOpenTelemetry.getTracer("fraud-engine");
    }
}