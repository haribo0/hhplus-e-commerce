package kr.hhplus.be.server.interfaces.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 API")
public class OrderController {

    @PostMapping
    @Operation(summary = "주문", description = "주문을 생성합니다.")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        int totalQuantity = request.items().stream().mapToInt(OrderRequest.Item::quantity).sum();
        long userId = request.userId();

        if (totalQuantity > 10) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new OrderResponse("주문 실패: 재고가 부족합니다", null));
        }

        if (userId == 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new OrderResponse("주문 실패: 잔액이 부족합니다", null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrderResponse("주문 성공", 1001L));
    }

}
