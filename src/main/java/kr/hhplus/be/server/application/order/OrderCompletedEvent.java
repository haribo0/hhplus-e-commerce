package kr.hhplus.be.server.application.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCompletedEvent {

    private Long orderId;
    private Long paymentId;

    @JsonCreator
    public OrderCompletedEvent(@JsonProperty("orderId") Long orderId,
                               @JsonProperty("paymentId") Long paymentId) {
        this.orderId = orderId;
        this.paymentId = paymentId;
    }

}
