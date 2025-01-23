package kr.hhplus.be.server.interfaces.coupon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupon", description = "쿠폰 API")
public class CouponController {

    @PostMapping
    @Operation(summary = "쿠폰 발급", description = "쿠폰을 발급합니다.")
    public ResponseEntity<CouponResponse> issueCoupon(@RequestBody CouponRequest request) {
        if ("1".equalsIgnoreCase(request.couponCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new CouponResponse("쿠폰 발급 실패: 이미 발급된 쿠폰입니다", null));
        }
        if ("2".equalsIgnoreCase(request.couponCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new CouponResponse("쿠폰 발급 실패: 쿠폰 수량이 모두 소진되었습니다", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CouponResponse("쿠폰 발급 성공", 101L)); // Mock coupon ID
    }

}
