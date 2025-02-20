package kr.hhplus.be.server.domain.order.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "order_data_outbox")
public class OrderDataOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    private int retryCount;

    public OrderDataOutbox(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.INIT;
        this.retryCount = 0; // 처음에는 0으로 초기화
    }

    public void markProcessed() {
        this.status = OutboxStatus.PROCESSED;
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
        this.retryCount += 1;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
