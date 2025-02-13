package kr.hhplus.be.server.application.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularProductCacheService {

    private static final String POPULAR_PRODUCTS_KEY = "popular_products";
    private static final long CACHE_TTL_HOURS = 24; // 캐시 유지 시간 (24시간)

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    /**
     * 인기 상품 목록을 Redis에 캐싱
     */
    public void cachePopularProducts(List<ProductInfo.PopularItem> popularProducts) {
        try {
            String json = objectMapper.writeValueAsString(popularProducts);
            RBucket<String> bucket = redissonClient.getBucket(POPULAR_PRODUCTS_KEY);
            bucket.set(json, CACHE_TTL_HOURS, TimeUnit.HOURS); // 24시간 유지
            log.info("인기 상품 캐싱 완료 ({}개)", popularProducts.size());
        } catch (Exception e) {
            log.error("인기 상품 캐싱 실패", e);
        }
    }

    /**
     * Redis에서 인기 상품 가져오기
     */
    public List<ProductInfo.PopularItem> getCachedPopularProducts() {
        try {
            RBucket<String> bucket = redissonClient.getBucket(POPULAR_PRODUCTS_KEY);
            String json = bucket.get();
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis에서 인기 상품 조회 실패", e);
        }
        return List.of();
    }
}
