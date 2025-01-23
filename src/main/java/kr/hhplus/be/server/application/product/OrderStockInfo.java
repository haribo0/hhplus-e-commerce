package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Stock;

public record OrderStockInfo(Stock stock, int orderQuantity) {
}
