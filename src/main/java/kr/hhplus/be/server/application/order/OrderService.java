package kr.hhplus.be.server.application.order;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final CouponRepository couponRepository;

    // 주문 생성
    @Transactional
    public Order create(OrderCommand.Create command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + command.userId()));

        Order order = Order.create(command.userId());

        List<Long> productIds = command.items().stream()
                .map(OrderCommand.OrderItem::productId)
                .collect(Collectors.toList());
        List<Stock> lockedStocks = stockRepository.findByProductIdsWithLock(productIds);
        log.info("stocklist size={}",lockedStocks.size());

        for (OrderCommand.OrderItem itemCommand : command.items()) {
            // 해당 상품의 재고 찾기
            Stock stock = lockedStocks.stream()
                    .filter(s -> s.getProduct().getId().equals(itemCommand.productId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("재고가 부족합니다. 상품 ID: " + itemCommand.productId()));
            if (stock.getQuantity() < itemCommand.quantity()) {
                throw new IllegalStateException("재고가 부족합니다. 상품 ID: " + itemCommand.productId());
            }
            Product product = stock.getProduct();
            OrderItem orderItem = new OrderItem(null, order, product, itemCommand.quantity(), product.getPrice());
            order.addOrderItem(orderItem); // 주문 항목 추가
        }

        // 주문 저장
        return orderRepository.save(order);

    }


}
