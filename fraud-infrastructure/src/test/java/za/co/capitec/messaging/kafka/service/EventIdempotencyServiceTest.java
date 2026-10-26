package za.co.capitec.messaging.kafka.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.capitec.persistence.ProcessedEventEntity;
import za.co.capitec.persistence.ProcessedEventJpaRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventIdempotencyServiceTest {

    @Mock
    private ProcessedEventJpaRepository processedEventJpaRepository;

    @InjectMocks
    private EventIdempotencyService service;

    @Test
    void shouldReturnTrueWhenEventAlreadyProcessed() {
        when(processedEventJpaRepository.existsById("evt-001")).thenReturn(true);

        boolean result = service.alreadyProcessed("evt-001");

        assertTrue(result);
        verify(processedEventJpaRepository).existsById("evt-001");
    }

    @Test
    void shouldSaveProcessedEventMarker() {
        service.markProcessed("evt-001", "ATM");

        ArgumentCaptor<ProcessedEventEntity> captor =
                ArgumentCaptor.forClass(ProcessedEventEntity.class);

        verify(processedEventJpaRepository).save(captor.capture());

        ProcessedEventEntity entity = captor.getValue();
        assertEquals("evt-001", entity.getEventId());
        assertEquals("ATM", entity.getSource());
        assertNotNull(entity.getProcessedAt());
    }
}