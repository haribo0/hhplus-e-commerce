package kr.hhplus.be.server.interfaces.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.application.order.OrderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 API")
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    @Operation(summary = "주문", description = "주문을 생성합니다.")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {

        OrderInfo order = orderFacade.order(OrderRequest.toCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrderResponse("주문 성공", order.orderId()));
    }

}
