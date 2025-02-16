package kr.hhplus.be.server.application.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularProductScheduler {

    private final ProductService productService;
    private final PopularProductCacheService popularProductCacheService;

    /**
     * 매일 자정(00:00)에 인기 상품을 Redis에 캐싱
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00 실행
    public void updatePopularProductsCache() {
        log.info("[스케줄러] 인기 상품 캐싱 시작...");
        List<ProductInfo.PopularItem> popularProducts = productService.getPopularProducts(10);
        popularProductCacheService.cachePopularProducts(popularProducts);
        log.info("[스케줄러] 인기 상품 캐싱 완료");
    }
}
