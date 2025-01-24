package kr.hhplus.be.server.domain.point;

import java.util.List;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);

    List<PointHistory> findAll();
}
