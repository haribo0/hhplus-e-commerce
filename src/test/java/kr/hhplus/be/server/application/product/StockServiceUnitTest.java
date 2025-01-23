package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class StockServiceUnitTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    private Stock stock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Product product = ProductFixture.product(1L,"아이폰16", BigDecimal.valueOf(1_999_999), "전자기기");
        stock = ProductFixture.stock(product, 100);  // 상품 ID: 1, 재고: 100
    }

    @Test
    @DisplayName("validateStocksWithLock을 호출하면 재고가 충분하면 성공한다")
    void validateStocksWithLock_ShouldSucceed_WhenStockIsSufficient() {
        // given
        OrderCommand.OrderItem orderItem = new OrderCommand.OrderItem(1L, 10); // 상품 ID: 1, 수량: 10
        OrderCommand.Create command = new OrderCommand.Create(1L, List.of(orderItem), 1L);

        // when
        when(stockRepository.findByProductIdsWithLock(List.of(1L)))
                .thenReturn(List.of(stock)); // 재고를 100으로 설정

        List<OrderStockInfo> result = stockService.validateStocksWithLock(command);

        // then
        assertThat(result)
                .isNotNull()
                .hasSize(1);
        assertThat(result.get(0).stock().getQuantity()).isEqualTo(100); // 재고가 100이어야 한다
        assertThat(result.get(0).orderQuantity()).isEqualTo(10); // 주문한 수량이 10이어야 한다
        verify(stockRepository, times(1)).findByProductIdsWithLock(List.of(1L));
    }

    @Test
    @DisplayName("validateStocksWithLock을 호출하면 재고가 부족하면 예외가 발생한다")
    void validateStocksWithLock_ShouldThrowException_WhenStockIsInsufficient() {
        // given
        OrderCommand.OrderItem orderItem = new OrderCommand.OrderItem(1L, 150); // 상품 ID: 1, 수량: 150
        OrderCommand.Create command = new OrderCommand.Create(1L, List.of(orderItem), 1L);

        // when
        when(stockRepository.findByProductIdsWithLock(List.of(1L)))
                .thenReturn(List.of(stock)); // 재고를 100으로 설정

        // then
        assertThatThrownBy(() -> stockService.validateStocksWithLock(command)) // 예외가 발생해야 한다
                .isInstanceOf(IllegalStateException.class);

        verify(stockRepository, times(1)).findByProductIdsWithLock(List.of(1L));
    }

    @Test
    @DisplayName("deductStock을 호출하면 재고가 차감된다")
    void deductStock_ShouldDeductStock_WhenValid() {
        // given
        OrderCommand.OrderItem orderItem = new OrderCommand.OrderItem(1L, 10); // 상품 ID: 1, 수량: 10
        List<OrderStockInfo> orderStockInfos = List.of(new OrderStockInfo(stock, 10)); // stock과 수량을 함께 전달

        // when
        stockService.deductStock(orderStockInfos);

        // then
        assertThat(stock.getQuantity()).isEqualTo(90); // 재고가 100에서 10 차감되어 90이어야 한다
        verify(stockRepository, times(1)).save(stock); // 재고 저장 확인
    }

    @Test
    @DisplayName("deductStock을 호출하면 재고가 부족하면 예외가 발생한다")
    void deductStock_ShouldThrowException_WhenStockIsInsufficient() {
        // given
        OrderCommand.OrderItem orderItem = new OrderCommand.OrderItem(1L, 150); // 상품 ID: 1, 수량: 150
        List<OrderStockInfo> orderStockInfos = List.of(new OrderStockInfo(stock, 150)); // 재고 100인데 주문 수량이 150

        // when
        assertThatThrownBy(() -> stockService.deductStock(orderStockInfos)) // 예외가 발생해야 한다
                .isInstanceOf(IllegalStateException.class);

        // then
        verify(stockRepository, times(0)).save(stock); // 재고 차감이 실패하므로 저장되지 않는다
    }

    @Test
    @DisplayName("deductStock을 호출하면 재고가 존재하지 않으면 예외가 발생한다")
    void deductStock_ShouldThrowException_WhenStockDoesNotExist() {
        // given
        OrderCommand.OrderItem orderItem = new OrderCommand.OrderItem(1L, 10); // 상품 ID: 1, 수량: 10
        Stock stock = new Stock(null,0);
        OrderStockInfo orderStockInfo = new OrderStockInfo(stock, 1);
        List<OrderStockInfo> orderStockInfos = List.of(orderStockInfo); // 재고 없음

        // when
        assertThatThrownBy(() -> stockService.deductStock(orderStockInfos)) // 재고가 존재하지 않으므로 예외 발생
                .isInstanceOf(IllegalStateException.class);

        // then
        verify(stockRepository, times(0)).save(any(Stock.class)); // 재고 차감이 되지 않으므로 저장되지 않는다
    }
}
