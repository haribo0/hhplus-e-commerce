package kr.hhplus.be.server.interfaces.point;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
@Tag(name = "Point", description = "포인트 API")
public class PointController {

    private final PointService pointService;


    @PostMapping("/charge")
    @Operation(summary = "포인트 충전", description = "포인트를 충전합니다.")
    public ResponseEntity<PointResponse.charge> chargePoints(@RequestBody PointRequest request) {
        int points = pointService.charge(request.toCommand()).getPoints();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PointResponse.charge("포인트가 충전되었습니다.",points)); // Mock balance
    }

    @PostMapping("/{userId}")
    @Operation(summary = "포인트 조회", description = "포인트를 조회합니다.")
    public ResponseEntity<PointResponse.view> viewPoints(@RequestBody PointRequest request, @PathVariable Long userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PointResponse.view(pointService.get(userId).getPoints())); // Mock balance
    }

}
