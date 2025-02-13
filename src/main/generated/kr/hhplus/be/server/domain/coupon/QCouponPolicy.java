package kr.hhplus.be.server.domain.coupon;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCouponPolicy is a Querydsl query type for CouponPolicy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCouponPolicy extends EntityPathBase<CouponPolicy> {

    private static final long serialVersionUID = -83750621L;

    public static final QCouponPolicy couponPolicy = new QCouponPolicy("couponPolicy");

    public final kr.hhplus.be.server.domain.base.QBaseEntity _super = new kr.hhplus.be.server.domain.base.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<java.math.BigDecimal> discountValue = createNumber("discountValue", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> expirationDate = createDateTime("expirationDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> issuedCount = createNumber("issuedCount", Integer.class);

    public final NumberPath<java.math.BigDecimal> maxDiscountAmount = createNumber("maxDiscountAmount", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> minOrderAmount = createNumber("minOrderAmount", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final EnumPath<CouponPolicyStatus> status = createEnum("status", CouponPolicyStatus.class);

    public final NumberPath<Integer> totalCount = createNumber("totalCount", Integer.class);

    public final EnumPath<CouponType> type = createEnum("type", CouponType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCouponPolicy(String variable) {
        super(CouponPolicy.class, forVariable(variable));
    }

    public QCouponPolicy(Path<? extends CouponPolicy> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCouponPolicy(PathMetadata metadata) {
        super(CouponPolicy.class, metadata);
    }

}

