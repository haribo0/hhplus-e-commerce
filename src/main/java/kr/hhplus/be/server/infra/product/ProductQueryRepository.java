package kr.hhplus.be.server.infra.product;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.application.product.ProductInfo;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.QOrder;
import kr.hhplus.be.server.domain.order.QOrderItem;
import kr.hhplus.be.server.domain.product.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

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

}

