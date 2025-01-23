package kr.hhplus.be.server.interfaces.cart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "장바구니 API")
public class CartController {

    @PostMapping
    @Operation(summary = "장바구니 추가", description = "상품을 장바구니에 추가합니다.")
    public ResponseEntity<String> addToCart(@RequestBody CartItemRequest request) {
        if (request.quantity() > 10) { // Mocking stock check
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("장바구니 추가 실패: 상품의 재고가 부족합니다.");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("장바구니 추가 성공");
    }

}
