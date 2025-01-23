package kr.hhplus.be.server.infra.product;

import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class StockRepositoryImpl implements StockRepository {

    private final StockJpaRepository jpaRepository ;

    @Override
    public Optional<Stock> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Stock save(Stock stock) {
        return jpaRepository.save(stock);
    }

    @Override
    public List<Stock> findByProductIdsWithLock(List<Long> productIds) {
        return jpaRepository.findByProductIdsWithLock(productIds);
    }

}
