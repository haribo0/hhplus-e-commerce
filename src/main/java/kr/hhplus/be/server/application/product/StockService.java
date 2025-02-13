package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public List<Stock> getStocksWithLock(List<Long> productIds){
        return stockRepository.findByProductIdsWithLock(productIds);
    }

    public List<OrderStockInfo> validateStocksWithLock(OrderCommand.Create command) {
        List<Long> productIds = command.items().stream()
                .map(OrderCommand.OrderItem::productId)
                .toList();

        List<Stock> stocks = stockRepository.findByProductIdsWithLock(productIds);

        // 재고 검증
        Map<Long, Stock> stockMap = stocks.stream()
                .collect(Collectors.toMap(stock -> stock.getProduct().getId(), stock -> stock)); // 여기서 productId로 변경

        List<OrderStockInfo> orderStockInfos = new ArrayList<>();

        for (OrderCommand.OrderItem item : command.items()) {
            Stock stock = stockMap.get(item.productId());
            if (stock == null || stock.getQuantity() < item.quantity()) {
                throw new IllegalStateException("상품 재고가 부족합니다. 상품 ID: " + item.productId());
            }
            // Stock과 주문 수량을 묶어서 반환
            orderStockInfos.add(new OrderStockInfo(stock, item.quantity()));
        }

        return orderStockInfos;
    }

    public void deductStock(List<OrderStockInfo> orderStockInfos) {
        for (OrderStockInfo orderStockInfo : orderStockInfos) {
            Stock stock = orderStockInfo.stock();
            int quantity = orderStockInfo.orderQuantity();

            if (stock == null) {
                throw new IllegalStateException("상품 재고가 존재하지 않습니다. 상품 ID: " + stock.getProduct().getId());
            }

            // 재고 차감
            stock.decrease(quantity);
            stockRepository.save(stock);
        }
    }


}
