package kr.hhplus.be.server.infra.dataplatform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 데이터 플랫폼
 */
@Slf4j
@Component
public class DataPlatformImpl implements DataPlaform {

    // 주문 정보 전송
    @Override
    public void publish(Long orderId, Long paymentId) {
        log.info("Publishing to Mock Data Platform. Order ID: {}, Payment ID: {}", orderId, paymentId);
    }
}