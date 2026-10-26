package za.co.capitec.messaging.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.capitec.persistence.ProcessedEventEntity;
import za.co.capitec.persistence.ProcessedEventJpaRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EventIdempotencyService {

    private final ProcessedEventJpaRepository processedEventJpaRepository;

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(String eventId) {
        return processedEventJpaRepository.existsById(eventId);
    }

    @Transactional
    public void markProcessed(String eventId, String source) {
        processedEventJpaRepository.save(
                new ProcessedEventEntity(eventId, source, Instant.now())
        );
    }
}
