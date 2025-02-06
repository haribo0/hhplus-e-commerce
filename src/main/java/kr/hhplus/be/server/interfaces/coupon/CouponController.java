package kr.hhplus.be.server.interfaces.coupon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.coupon.CouponCommand;
import kr.hhplus.be.server.application.coupon.CouponInfo;
import kr.hhplus.be.server.application.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupon", description = "쿠폰 API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "쿠폰 발급", description = "쿠폰을 발급합니다.")
    public ResponseEntity<CouponResponse> issueCoupon(@RequestBody CouponRequest request) {
        couponService.request(CouponRequest.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CouponResponse("쿠폰 발급이 성공적으로 요청되었습니다.")); // Mock coupon ID
    }

}
