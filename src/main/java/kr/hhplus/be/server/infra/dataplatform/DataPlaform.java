package kr.hhplus.be.server.infra.dataplatform;

import org.springframework.stereotype.Component;

/**
 * 데이터 플랫폼
 */
@Component
public interface DataPlaform {

    // 주문 정보 전송
    void publish(Long orderId, Long paymentId);

}