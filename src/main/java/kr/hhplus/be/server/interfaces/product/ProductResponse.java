package kr.hhplus.be.server.interfaces.product;


import kr.hhplus.be.server.application.product.ProductInfo;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price) {
    public static ProductResponse from(ProductInfo.Item productInfo) {
        return new ProductResponse(
                productInfo.id(),
                productInfo.name(),
                productInfo.price()
        );
    }

}
