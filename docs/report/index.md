## 인덱스 적용 성능 개선

---

### 1. **개요**

E커머스 시스템에서 **상품 조회 성능**은 매우 중요한 요소입니다. 특히 **인기 상품 조회**와 같은 복잡한 쿼리는 자주 호출되며, 성능 최적화가 필수적입니다. 본 보고서에서는 **`ProductQueryRepository`**에 정의된 **인기 상품 조회 쿼리**에 인덱스를 추가하고, **인덱스 적용 전후** 성능 개선을 분석한 결과를 다룹니다.

### 2. **인덱스란?**

인덱스는 특정 열에 대한 **빠른 검색**을 가능하게 하는 자료구조입니다. 인덱스를 사용하면 **전체 테이블을 스캔**하는 대신, 데이터베이스가 인덱스를 활용하여 필요한 데이터를 더 빠르게 조회할 수 있습니다. 데이터가 많을수록 인덱스의 **효과**는 더욱 두드러집니다.

### 3. **쿼리 설명**

**`findPopularProductsLast30Days`** 메서드는 **최근 30일** 동안의 **유료 주문**에서 **주문 수량**을 기준으로 상위 상품을 조회하는 쿼리입니다. 이 쿼리는 **`orderItem`**, **`product`**, **`order`** 테이블을 **조인**하여 상품 정보를 집계하고, 이를 기준으로 인기 상품을 조회합니다.

**쿼리 코드**:

```java
public List<ProductInfo.PopularItem> findPopularProductsLast30Days(int limit) {
    QProduct product = QProduct.product;
    QOrder order = QOrder.order;
    QOrderItem orderItem = QOrderItem.orderItem;

    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

    return queryFactory
            .select(Projections.constructor(
                    ProductInfo.PopularItem.class,
                    product.id,
                    product.name,
                    product.price,
                    product.description,
                    orderItem.count().as("orderCount")
            ))
            .from(orderItem)
            .join(orderItem.product, product)
            .join(orderItem.order, order)
            .where(
                    order.status.in(OrderStatus.PAID),
                    order.createdAt.after(thirtyDaysAgo)
            )
            .orderBy(orderItem.count().desc())
            .groupBy(product.id, product.name, product.price, product.description)
            .limit(limit)
            .fetch();
}

```

이 쿼리는 **최근 30일** 동안 **`PAID` 상태**인 주문을 기준으로 **주문 수량**을 기준으로 인기 상품을 조회합니다.

### 4. **인덱스 적용 계획**

쿼리에서 **주문 상태**와 **주문 날짜**는 **WHERE 절**에서 자주 사용됩니다. 또한 **`orderItem.product_id`**와 **`orderItem.order_id`**는 **조인**에 사용되므로 해당 컬럼에 인덱스를 추가하는 것이 적절하다고 생각했습니다.

**인덱스 적용 대상**:

1. **주문 테이블 (`orders`)**: `created_at`에 인덱스를 추가하여 **주문 날짜 범위**로 효율적으로 필터링합니다.
2. **주문 상세 테이블 (`order_item`)**: `product_id`와 `order_id`에 인덱스를 추가하여 **조인 성능**을 개선합니다.

### 5. **인덱스 설정**

쿼리 성능을 최적화하기 위해 아래와 같은 인덱스를 설정했습니다:

- **주문 테이블 (`orders`)**:
    - `created_at`에 인덱스를 추가하여 **날짜 범위**를 기준으로 주문을 효율적으로 필터링합니다.
- **주문 상세 테이블 (`order_item`)**:
    - `product_id`와 `order_id`에 인덱스를 추가하여 **조인 성능**을 개선합니다.

**인덱스 추가 쿼리**:

```sql
-- 주문 테이블에 인덱스 추가
CREATE INDEX idx_order_created_at ON orders (created_at);

-- 주문 상세 테이블에 인덱스 추가
CREATE INDEX idx_order_item_product_id ON order_item (product_id);
CREATE INDEX idx_order_item_order_id ON order_item (order_id);

```

### 6. **실행 계획 분석 (인덱스 추가 전후)**

### 6.1. **인덱스 추가 전 실행 계획**

**실행 계획**:

EXPLAIN

| id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | SIMPLE | orderItem | None | ALL | idx_order_item_order_id, idx_order_item_product_id | idx_order_item_order_id, idx_order_item_product_id | 2396175 | None | 2396175 | 100.0 | Using temporary; Using filesort |
| 1 | SIMPLE | product | None | eq_ref | PRIMARY | PRIMARY | 8 | hhplus.orderItem.product_id | 1 | 100.0 | None |
| 1 | SIMPLE | orders | None | eq_ref | PRIMARY | PRIMARY | 8 | hhplus.orderItem.order_id | 1 | 8.33 | Using where |

**분석**:

- *전체 테이블 스캔 (ALL)**이 발생하며, **임시 테이블**과 **파일 정렬 (Using filesort)**이 사용되어 성능이 저하되었습니다.
- **필터링 후 남은 행의 비율**은 5.56%로 비효율적인 필터링이 이루어지고 있음을 확인할 수 있습니다.

### 6.2. **인덱스 추가 후 실행 계획**

**실행 계획**:

EXPLAIN

| id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | SIMPLE | orderItem | None | ALL | idx_order_item_order_id, idx_order_item_product_id | idx_order_item_order_id, idx_order_item_product_id | 2396175 | None | 2396175 | 100.0 | Using temporary; Using filesort |
| 1 | SIMPLE | product | None | eq_ref | PRIMARY | PRIMARY | 8 | hhplus.orderItem.product_id | 1 | 100.0 | None |
| 1 | SIMPLE | orders | None | eq_ref | idx_order_created_at | idx_order_created_at | 8 | hhplus.orderItem.order_id | 1 | 50.0 | Using where |

**분석**:

- *`Using temporary; Using filesort`*가 여전히 발생하지만, **`orders` 테이블에서 `created_at`에 대한 인덱스**가 잘 활용되고 있습니다.
- *`Using where`*가 사용되지만, 인덱스를 통해 **`created_at` 필터링**이 더욱 효율적으로 처리되었습니다.

### 7. **성능 개선 비교**

### 7.1. **인덱스 추가 전 실행 시간**:

- **실행 시간**: `actual time=34.5..34.5` 초
- **문제점**: 전체 테이블 스캔과 파일 정렬이 발생하면서 성능 저하가 발생했습니다.

### 7.2. **인덱스 추가 후 실행 시간**:

- **실행 시간**: `actual time=7..7` 초
- **성능 개선**: **쿼리 성능이 80% 이상 향상**되었습니다. 인덱스를 통해 **정렬**과 **그룹화**가 훨씬 효율적으로 처리되었습니다.

### 8. **결론**

인덱스를 추가함으로써 **쿼리 성능이 크게 개선**되었습니다. 특히 **`orders.created_at`** 필드에 추가한 인덱스가 **가장 큰 성능 향상**을 보였습니다. 이 인덱스는 **주문 날짜**를 기준으로 **효율적인 필터링**을 가능하게 했고, 전체 테이블 스캔을 줄여주었습니다. 반면 **`order_item.product_id`**와 **`order_item.order_id`**에 추가한 인덱스는 성능 개선에 미미한 영향을 미쳤습니다. 이러한 결과는 **데이터의 분포**와 **조인 구조**에 따라 인덱스의 효율성이 달라짐을 보여줍니다. 특히 **`product_id`**와 **`order_id`**에 대한 인덱스는 이미 많은 **중복 데이터**가 존재하는 경우 성능 향상이 제한적일 수 있음을 나타냅니다. 그럼에도 **두 가지 인덱스를 없이 주문 날짜 인덱스만 설정**한 경우 **실행시간이 9.53**초인 것을 고려하면 여전히 두 컬럼에도 인덱스를 설정하는 것이 좋다는 결론을 내렸습니다.

따라서, **읽기 성능이 중요한 쿼리**에서는 **주문 날짜 필터링**처럼 **자주 사용되는 범위 조건**에 대해 인덱스를 추가하는 것이 효율적일 수 있습니다.