package kr.hhplus.be.server.application.point;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.point.*;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    private final String POINT_USE_DESC = "포인트 사용";
    private final String POINT_CHARGE_DESC = "포인트 충전";

    // 일반 조회용: 락 없이 동작
    public Point get(Long userId) {

        Optional<Point> pointOptional = pointRepository.findByUserId(userId);
        if (pointOptional.isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            Point newPoint = new Point(userId);
            pointRepository.save(newPoint); // 저장 후 반환
            return newPoint;
        }
        return pointOptional.get();
    }


    // 포인트 충전
    @Transactional
    public Point charge(PointCommand.Charge command) {

        try {
            Long userId = command.userId();
            Point point = pointRepository.findByUserIdWithLock(userId)
                    .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
                        Point newPoint = new Point(userId);
                        pointRepository.save(newPoint); // 저장 후 반환
                        return newPoint;
                    });
            point.charge(command.amount());
            pointRepository.save(point);
            PointHistory pointHistory = PointHistory.createPointHistory(point, PointHistoryType.USE, command.amount(), POINT_CHARGE_DESC);
            pointHistoryRepository.save(pointHistory);
        return point;
        } catch (OptimisticLockException e) {
            throw new RuntimeException("이미 다른 요청이 처리 중입니다. 잠시 후 다시 시도해 주세요.", e);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("포인트 충전 중 문제가 발생했습니다.", e);
        }
    }

    // 포인트 사용
    @Transactional
    public Point use(PointCommand.Use command){
        try{
            Point point = pointRepository.findByUserIdWithLock(command.userId())
                    .orElseThrow(()->new IllegalArgumentException("사용할 포인트가 없습니다."));
            point.use(command.amount());
            pointRepository.save(point);
            PointHistory pointHistory = PointHistory.createPointHistory(point, PointHistoryType.USE, command.amount(), POINT_USE_DESC);
            pointHistoryRepository.save(pointHistory);
            return point;
        } catch (OptimisticLockException e) {
            throw new RuntimeException("이미 다른 요청이 처리 중입니다. 잠시 후 다시 시도해 주세요.", e);
        } catch (Exception e) {
            throw new RuntimeException("포인트 사용처리 중 문제가 발생했습니다.", e);
        }
    }



}
