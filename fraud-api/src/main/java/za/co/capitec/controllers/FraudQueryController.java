package za.co.capitec.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import za.co.capitec.domain.model.FraudAlert;
import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.dto.FraudAlertResponse;
import za.co.capitec.dto.FraudCheckResponse;
import za.co.capitec.dto.RuleResultResponse;
import za.co.capitec.usecases.GetFraudAlertsUseCase;
import za.co.capitec.usecases.GetFraudCheckUseCase;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class FraudQueryController {

    private final GetFraudCheckUseCase getFraudCheckUseCase;
    private final GetFraudAlertsUseCase getFraudAlertsUseCase;

    public FraudQueryController(
            GetFraudCheckUseCase getFraudCheckUseCase,
            GetFraudAlertsUseCase getFraudAlertsUseCase
    ) {
        this.getFraudCheckUseCase = getFraudCheckUseCase;
        this.getFraudAlertsUseCase = getFraudAlertsUseCase;
    }

    @GetMapping("/transactions/{transactionId}/fraud-check")
    public FraudCheckResponse getFraudCheck(@PathVariable UUID transactionId) {
        FraudCheck fraudCheck = getFraudCheckUseCase.getByTransactionId(transactionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fraud check not found for transactionId: " + transactionId
                ));

        return toFraudCheckResponse(fraudCheck);
    }

    @GetMapping("/fraud-alerts")
    public List<FraudAlertResponse> getFraudAlerts(
            @RequestParam(name = "transactionId", required = false) UUID transactionId
    ) {
        List<FraudAlert> alerts = transactionId == null
                ? getFraudAlertsUseCase.getAll()
                : getFraudAlertsUseCase.getByTransactionId(transactionId);

        return alerts.stream()
                .map(this::toFraudAlertResponse)
                .toList();
    }

    private FraudCheckResponse toFraudCheckResponse(FraudCheck fraudCheck) {
        return new FraudCheckResponse(
                fraudCheck.transactionId(),
                fraudCheck.riskScore(),
                fraudCheck.riskLevel().name(),
                fraudCheck.isFraudSuspected(),
                fraudCheck.evaluatedAt(),
                fraudCheck.ruleResults().stream()
                        .map(ruleResult -> new RuleResultResponse(
                                ruleResult.ruleName(),
                                ruleResult.triggered(),
                                ruleResult.score(),
                                ruleResult.reason()
                        ))
                        .toList()
        );
    }

    private FraudAlertResponse toFraudAlertResponse(FraudAlert fraudAlert) {
        return new FraudAlertResponse(
                fraudAlert.id(),
                fraudAlert.transactionId(),
                fraudAlert.riskScore(),
                fraudAlert.riskLevel().name(),
                fraudAlert.triggeredRules(),
                fraudAlert.createdAt()
        );
    }
}
