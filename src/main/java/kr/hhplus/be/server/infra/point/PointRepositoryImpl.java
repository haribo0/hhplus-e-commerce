package kr.hhplus.be.server.infra.point;

import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository jpaRepository;


    @Override
    public Point save(Point point) {
        return jpaRepository.save(point);
    }

    @Override
    public Optional<Point> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Point> findByUserId(Long userId) {
        return jpaRepository.findById(userId);
    }
    @Override
    public Optional<Point> findByUserIdWithLock(Long userId) {
        return jpaRepository.findByUserIdWithLock(userId);
    }


}
