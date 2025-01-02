package kr.hhplus.be.server.interfaces.order;


import java.util.List;

public record OrderRequest(Long userId, List<Item> items) {

    public record Item(Long productId, int quantity) {}
}
