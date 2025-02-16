인덱스 적용 성능 개선

1. 개요

E커머스 시스템에서 상품 조회 성능은 매우 중요한 요소입니다. 특히 인기 상품 조회와 같은 복잡한 쿼리는 자주 호출되며, 성능 최적화가 필수적입니다. 본 보고서에서는 ProductQueryRepository에 정의된 인기 상품 조회 쿼리에 인덱스를 추가하고, 인덱스 적용 전후 성능 개선을 분석한 결과를 다룹니다.

2. 문제 상황 및 인덱스 선택 이유

문제점

인기 상품 조회 시 최근 30일 내 주문 정보를 필터링해야 하며, 주문 상태가 PAID인 데이터만 조회해야 합니다.

order_item 테이블과 product 테이블, order 테이블 간의 조인 연산이 포함되어 있어 조인 및 필터링 성능 저하가 발생합니다.

기존 쿼리는 **전체 테이블 스캔(Full Table Scan)**이 발생하여 실행 속도가 느립니다.

**파일 정렬(Using Filesort)과 임시 테이블(Using Temporary)**이 사용되어 성능 저하가 두드러집니다.

인덱스 설정이 가장 먼저 고려되어야 하는 이유

비용 대비 효과가 큽니다. 인덱스 설정은 코드 수정 없이도 성능을 크게 개선할 수 있는 가장 쉬운 방법 중 하나입니다.

데이터베이스 최적화의 기본 기법입니다. 대부분의 성능 문제는 불필요한 전체 테이블 스캔에서 발생하며, 인덱스를 통해 이를 방지할 수 있습니다.

다른 최적화 기법의 기반이 됩니다. 인덱스가 없으면 캐싱, 쿼리 리팩토링, 파티셔닝과 같은 추가적인 최적화 기법도 효과적으로 작동하지 않습니다.

데이터 증가에 대비할 수 있습니다. 시간이 지나면서 데이터가 많아질수록 전체 테이블 스캔의 부하가 기하급수적으로 증가하기 때문에, 사전에 인덱스를 설정하는 것이 필수적입니다.

3. 쿼리 설명

findPopularProductsLast30Days 메서드는 최근 30일 동안의 유료 주문에서 주문 수량을 기준으로 상위 상품을 조회하는 쿼리입니다. 이 쿼리는 orderItem, product, order 테이블을 조인하여 상품 정보를 집계하고, 이를 기준으로 인기 상품을 조회합니다.

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

4. 인덱스 필드 선정 및 근거

4.1. 인덱스 설정 기준

카디널리티(데이터 중복도) 고려

**높은 카디널리티(중복도가 낮은 데이터)**는 인덱스를 통한 필터링 효과가 큽니다.

**낮은 카디널리티(중복도가 높은 데이터)**는 단순 인덱스보다는 복합 인덱스를 적용해야 효과적입니다.

조회 패턴 분석을 통해 자주 사용되는 조건문 및 조인 키를 기반으로 인덱스를 설정해야 합니다.

정렬 및 그룹화 최적화를 위해 쿼리에서 ORDER BY 및 GROUP BY가 자주 사용되는 경우, 해당 컬럼을 포함한 인덱스를 활용하는 것이 중요합니다.

4.2. orders.created_at 인덱스

근거

WHERE 절에서 created_at이 최근 30일 기준으로 필터링됩니다.

**범위 검색(Range Scan)**을 최적화하여 불필요한 데이터 조회를 최소화합니다.

카디널리티가 높아 필터링 효과가 좋습니다.

4.3. order_item.product_id, order_item.order_id 인덱스

근거

order_item 테이블에서 product 및 order 테이블과 조인됩니다.

조인 키에 인덱스를 추가하면 탐색 속도가 빨라지고 해시 조인(Hash Join) 대신 인덱스 탐색(Index Seek)을 사용할 수 있습니다.

카디널리티가 낮은 경우, 단순 인덱스보다 복합 인덱스를 활용하는 것이 효과적입니다.

5. 결론 및 추가 개선 방안

읽기 성능이 중요한 쿼리에서는 인덱스 활용이 필수적입니다.

추가적인 최적화 방안

커버링 인덱스(Covering Index) 적용 고려 (order_id, created_at 복합 인덱스 활용).

인덱스 사용률 모니터링을 통해 불필요한 인덱스를 제거.

쿼리 리팩토링을 통한 추가 최적화 검토.

이러한 개선을 통해 E커머스 시스템의 인기 상품 조회 성능을 최적화할 수 있습니다.

