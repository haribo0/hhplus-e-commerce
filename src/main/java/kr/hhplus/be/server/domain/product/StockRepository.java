package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.infra.product.StockJpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository {

    Optional<Stock> findById(Long id);

    Stock save(Stock stock);

    List<Stock> findByProductIdsWithLock(List<Long> productIds);


}
