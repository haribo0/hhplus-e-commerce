package kr.hhplus.be.server.application.order;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.coupon.CouponService;
import kr.hhplus.be.server.application.payment.PaymentCommand;
import kr.hhplus.be.server.application.payment.PaymentService;
import kr.hhplus.be.server.application.point.PointCommand;
import kr.hhplus.be.server.application.point.PointService;
import kr.hhplus.be.server.application.product.OrderStockInfo;
import kr.hhplus.be.server.application.product.ProductService;
import kr.hhplus.be.server.application.product.StockService;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.infra.dataplatform.DataPlaform;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final PointService pointService;
    private final CouponService couponService;
//    private final DataPlaform dataPlatform;
    private final KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;


    @Transactional
    public OrderInfo order(OrderCommand.Create command){

        // 재고 조회(withLock)
        List<OrderStockInfo> orderStockInfos = stockService.validateStocksWithLock(command);
        // 주문 생성
        Order order = orderService.create(command);

        // 쿠폰 적용
        if(command.couponId() != null){
            BigDecimal discountAmount = couponService.use(order.getTotalAmount(), command.couponId());
            order.applyDiscount(discountAmount);
        }

        // 결제
        Payment payment = paymentService.create(new PaymentCommand.Create(order.getId(), order.getTotalAmount()));
        pointService.use(new PointCommand.Use(command.userId(), payment.getAmount().intValue()));
        paymentService.changeStatus(payment,PaymentStatus.SUCCESS);

        // 주문 상태변경
        orderService.updateStatus(order,OrderStatus.PAID);

        // 재고차감
        stockService.deductStock(orderStockInfos);

        // 외부 api 호출

        // Kafka 이벤트 발행 (데이터 플랫폼 서비스로 주문 정보 전달)
        kafkaTemplate.send("order.completed", new OrderCompletedEvent(order.getId(), payment.getId()));

        return new OrderInfo(order.getId());


    }



}
