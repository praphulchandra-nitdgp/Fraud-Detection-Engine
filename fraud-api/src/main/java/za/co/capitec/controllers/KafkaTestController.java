package za.co.capitec.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import za.co.capitec.domain.model.Transaction;
import za.co.capitec.dto.TransactionEventMessageRequest;
import za.co.capitec.messaging.kafka.dto.TransactionEventMessage;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/kafka-test")
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaTemplate<String, TransactionEventMessage> kafkaTemplate;

    @PostMapping("/transactions")
    public ResponseEntity<String> publish(@RequestBody TransactionEventMessageRequest payload) {
        TransactionEventMessage event = new TransactionEventMessage(
                UUID.randomUUID().toString(),
                "API-SIMULATOR",
                Instant.now(),
                new Transaction(
                        UUID.randomUUID(),
                        payload.transactionRequest().accountId(),
                        payload.transactionRequest().amount(),
                        payload.transactionRequest().merchant(),
                        payload.transactionRequest().country(),
                        payload.transactionRequest().accountHomeCountry(),
                        Instant.now()
                )
        );

        kafkaTemplate.send("fraud.transactions.v1", event.eventId(), event);
        return ResponseEntity.ok("Published event " + event.eventId());
    }
}