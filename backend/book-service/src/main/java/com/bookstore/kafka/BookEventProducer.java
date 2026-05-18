package com.bookstore.kafka;

import com.bookstore.common.event.BookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for publishing book events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventProducer {

    private final KafkaTemplate<String, BookEvent> kafkaTemplate;

    @Value("${kafka.topic.book-events}")
    private String bookEventsTopic;

    /**
     * Publish book event to Kafka
     */
    public void publishBookEvent(BookEvent event) {
        log.info("Publishing book event: {} for book ID: {}", event.getEventType(), event.getBookId());
        
        CompletableFuture<SendResult<String, BookEvent>> future = 
            kafkaTemplate.send(bookEventsTopic, event.getBookId().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published book event: {} for book ID: {} to partition: {}", 
                    event.getEventType(), 
                    event.getBookId(),
                    result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish book event: {} for book ID: {}", 
                    event.getEventType(), 
                    event.getBookId(), 
                    ex);
            }
        });
    }
}

// Made with Bob
