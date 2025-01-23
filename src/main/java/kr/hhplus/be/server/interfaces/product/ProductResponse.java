package kr.hhplus.be.server.interfaces.product;

public record ProductResponse(Long id, String name, String category, int price, int stock) {}
