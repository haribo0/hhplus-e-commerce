package kr.hhplus.be.server.interfaces.point;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/points")
@Tag(name = "Point", description = "포인트 API")
public class PointController {

    @PostMapping("/charge")
    @Operation(summary = "포인트 충전", description = "포인트를 충전합니다.")
    public ResponseEntity<PointResponse.charge> chargePoints(@RequestBody PointRequest request) {
        if (request.amount() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PointResponse.charge("충전 실패: 유효하지 않은 금액입니다", 0));
        }
        if (request.amount() >= 10000000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PointResponse.charge("충전 실패: 최대 보유 포인트 이상 충전할 수 없습니다", 0));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PointResponse.charge("포인트 충전 성공",request.amount() + 10000)); // Mock balance
    }

    @PostMapping("/{userId}")
    @Operation(summary = "포인트 조회", description = "포인트를 조회합니다.")
    public ResponseEntity<PointResponse.view> viewPoints(@RequestBody PointRequest request, @PathVariable Long userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PointResponse.view(10000)); // Mock balance
    }

}
