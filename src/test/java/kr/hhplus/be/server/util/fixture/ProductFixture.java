package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.product.Category;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.Stock;

import java.math.BigDecimal;

public record ProductFixture() {

    public static Product product(Long id, String name, BigDecimal price, String category){
        return Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .description(name)
                .categoryId(1L)
                .build();
    }

    public static Product product(String name, BigDecimal price, String category){
        return Product.builder()
                .name(name)
                .price(price)
                .description(name)
                .categoryId(1L)
                .build();
    }

    public static Category category(String name){
        return Category.builder()
                .name(name)
                .isActive(true)
                .build();
    }

    public static Stock stock(Product product, int quantity){
        return Stock.builder()
                .product(product)
                .quantity(quantity)
                .build();
    }



}
