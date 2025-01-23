package kr.hhplus.be.server.application.product;


import java.math.BigDecimal;

public record ProductInfo(Long id, String name, BigDecimal price, int quantity, String description) {

    public record Item(Long id, String name, BigDecimal price, String description) {}

    public record ItemDetail(Long id, String name, BigDecimal price, String description, int quantity) {

    }
}
