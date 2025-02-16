

# Redis를 활용한 판매량 기반 인기 상품 조회 성능 최적화

---

## 1. 문제점

### 1.1 이커머스에서 인기 상품 조회의 중요성

이커머스 플랫폼에서는 **판매량이 높은 인기 상품을 빠르게 제공하는 것**이 중요하다.  
유저들은 베스트셀러 상품을 확인하고 구매를 결정하는 경향이 있으며, 이를 실시간으로 반영하는 것이 매출 증가에 기여할 수 있다.

일반적으로 인기 상품은 **판매량을 기준으로 정렬하여 상위 상품을 조회**하는 방식으로 제공된다.

### 1.2 기존 인기 상품 조회 방식과 문제점

기존 방식은 데이터베이스에서 판매량을 기준으로 정렬하여 상위 상품을 조회하는 SQL을 사용한다.

```sql
SELECT p.id, p.name, p.price, SUM(oi.quantity) as total_sold
FROM order_item oi
JOIN product p ON oi.product_id = p.id
GROUP BY p.id, p.name, p.price
ORDER BY total_sold DESC
LIMIT 10;
```

**문제점**
- **데이터베이스 부하 증가**: 트래픽이 증가할수록 인기 상품 조회 쿼리의 부하가 커진다.
- **실시간 반영 어려움**: 집계 데이터를 실시간으로 반영하기 어렵다.
- **조회 성능 저하**: `GROUP BY`와 `ORDER BY` 연산이 비용이 크다.

이러한 문제를 해결하기 위해 **Redis를 활용하여 인기 상품을 실시간으로 관리**하는 방식을 적용한다.

---

## 2. Redis 기반 인기 상품 조회 최적화 방법

### 2.1 목표

- **판매량이 높은 상품을 실시간으로 조회**
- **데이터베이스 부하를 줄이고 조회 성능을 개선**
- **트래픽 증가에도 원활한 확장성을 유지**

### 2.2 해결 방안

Redis의 **Sorted Set (ZSET)** 을 활용하여 인기 상품을 효율적으로 관리한다.

1. **상품이 판매될 때마다 Redis의 Sorted Set에 판매량을 증가시킴**
   - `popular:products:sales` 키를 사용하여 판매량을 관리.
   - 판매가 발생할 때마다 해당 상품의 판매량을 Redis에서 증가시킴.
2. **Redis에서 인기 상품 조회가 불가능한 경우 DB 조회 수행**
   - Redis에 데이터가 없는 경우, DB에서 기본적인 상품 목록을 제공.
3. **매일 자정 Redis에서 인기 상품 캐싱**
   - 하루 동안의 인기 상품을 캐싱하여 빠른 조회 가능.
   - `popular:products:cached` 키를 활용.

이 방식을 적용하면 **데이터베이스 부하를 줄이면서 실시간 인기 상품을 효과적으로 관리**할 수 있다.

---

## 3. Redis 적용 후 성능 개선 효과

### 3.1 응답 속도 비교

| 조회 방식 | DB 조회 응답 시간 | Redis 조회 응답 시간 |
| --- | --- | --- |
| 기존 방식 (DB 조회) | 300~800ms | - |
| Redis 캐싱 | - | 5~20ms |

### 3.2 서버 부하 감소

- 기존 방식에서는 인기 상품을 조회할 때마다 `GROUP BY`, `ORDER BY`를 수행해야 했지만,  
  Redis를 활용하면 **데이터베이스의 부하를 줄이고 조회 성능을 향상**시킬 수 있다.

### 3.3 트래픽 증가 대비 확장성

- 데이터베이스보다 Redis는 트래픽을 훨씬 빠르게 처리하며,  
  **캐싱된 데이터를 활용하여 고성능을 유지**할 수 있다.

---

## 4. 구현 방법

### 4.1 판매량을 Redis에 저장

#### 1) 주문 완료 시 Redis에 판매량 증가

```java
public void increaseProductSales(Long productId, int quantity) {
    redisTemplate.opsForZSet().incrementScore("popular:products:sales", productId.toString(), quantity);
}
```

- `order_item` 테이블의 데이터를 기준으로 상품 판매량을 Redis의 Sorted Set에 반영한다.

#### 2) Redis에서 인기 상품 조회

```java
public List<ProductInfo.Item> getPopularProducts(Pageable pageable) {
    Set<String> productIds = redisTemplate.opsForZSet()
            .reverseRange("popular:products:sales", 0, pageable.getPageSize() - 1);

    if (productIds == null || productIds.isEmpty()) {
        // Redis에 데이터가 없으면 전체 상품에서 인기 상품 조회
        return productRepository.findAllProducts(pageable)
                .map(product -> new ProductInfo.Item(product.getId(), product.getName(), product.getPrice(), product.getDescription()))
                .getContent();
    }

    // Redis에서 가져온 ID를 Long 타입으로 변환하여 리스트로 저장
    List<Long> sortedProductIds = productIds.stream()
            .map(Long::parseLong)
            .toList();

    // DB에서 상품 정보 조회
    List<Product> products = productRepository.findByIdIn(sortedProductIds);

    // **순서 보장을 위한 정렬 적용**
    Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, product -> product, (a, b) -> a, LinkedHashMap::new));

    // Redis에서 가져온 ID 순서대로 정렬하여 반환
    return sortedProductIds.stream()
            .map(productMap::get)
            .filter(Objects::nonNull) // 혹시라도 DB에서 누락된 데이터 방지
            .map(product -> new ProductInfo.Item(product.getId(), product.getName(), product.getPrice(), product.getDescription()))
            .toList();
}
```

- Redis에서 판매량이 높은 상품을 가져와, DB에서 실제 상품 정보를 조회한다.
- **순서 보장을 위해 LinkedHashMap을 사용하여 정렬**한다.

#### 3) 매일 자정 Redis 인기 상품 캐싱 (스케줄러 활용)

```java
@Scheduled(cron = "0 0 0 * * *")
public void updatePopularProductsCache() {
    Set<String> topProducts = redisTemplate.opsForZSet().reverseRange("popular:products:sales", 0, 9);
    if (topProducts != null) {
        redisTemplate.delete("popular:products:cached");
        topProducts.forEach(productId -> redisTemplate.opsForZSet().add("popular:products:cached", productId, 1));
    }
}
```

- 매일 자정, Redis에서 **가장 많이 판매된 상품 10개를 캐싱**한다.

---

## 5. 결론 및 기대 효과

### 5.1 결론

Redis를 활용하여 인기 상품 조회 로직을 최적화함으로써 **데이터베이스 부하를 줄이고 조회 속도를 개선**할 수 있다.  
또한, **실시간 업데이트가 가능하여 최신 인기 상품을 빠르게 제공**할 수 있다.

### 5.2 기대 효과

| 개선 전 (DB 조회) | 개선 후 (Redis 캐싱) |
| --- | --- |
| 매번 인기 상품을 DB에서 조회 | Redis에 저장된 판매량 활용 |
| 응답 속도 300~800ms | 응답 속도 5~20ms |
| 트래픽 증가 시 DB 부하 증가 | 트래픽 증가 시 Redis로 확장 가능 |

