package za.co.capitec.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import za.co.capitec.domain.model.FraudCheck;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.dto.FraudCheckResponse;
import za.co.capitec.dto.RuleResultResponse;
import za.co.capitec.dto.TransactionRequest;
import za.co.capitec.usecases.ProcessTransactionUseCase;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final ProcessTransactionUseCase processTransactionUseCase;

    public TransactionController(ProcessTransactionUseCase processTransactionUseCase) {
        this.processTransactionUseCase = processTransactionUseCase;
    }

    @PostMapping
    public FraudCheckResponse process(@Valid @RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                request.accountId(),
                request.amount(),
                request.merchant(),
                request.country(),
                request.accountHomeCountry(),
                Instant.now()
        );

        FraudCheck fraudCheck = processTransactionUseCase.process(transaction);
        return toResponse(fraudCheck);
    }

    private FraudCheckResponse toResponse(FraudCheck fraudCheck) {
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
}