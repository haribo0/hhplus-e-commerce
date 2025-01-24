package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.*;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ProductServiceUnitTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProductService productService;

    private static Category parentCategory ;

    @BeforeAll
    private static void setBeforeAll(){
        // Arrange
        Category parentCategory = Category.builder()
                .name("전자기기")
                .description("전자 제품 카테고리")
                .isActive(true)
                .build();

        Category childCategory = Category.builder()
                .name("스마트폰")
                .description("스마트폰 카테고리")
                .isActive(true)
                .parent(parentCategory)
                .build();

    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("전체 상품 목록 조회 시 전체 상품 목록을 페이지 단위로 반환한다")
    void getAllProducts_shouldReturnProductPage() {
        // Arrange
        Category category = parentCategory;
        Product product = new Product(1L, "스마트폰", BigDecimal.valueOf(1000), "최신 스마트폰", 1L);
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findAllProducts(any())).thenReturn(productPage);

        // Act
        Page<ProductInfo.Item> result = productService.getAllProducts(PageRequest.of(0, 10));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("스마트폰");
        assertThat(result.getContent().get(0).price()).isEqualTo(1000);
    }

    @Test
    @DisplayName("카테고리별 상품 목록 조회 테스트 - 페이징 처리")
    void getProductsByCategory_shouldReturnProductPage() {
        // Arrange
        Category category = new Category(1L,"전자기기","전자기기",true,null, new ArrayList<>());

        Product product = new Product(2L, "노트북", BigDecimal.valueOf(2000), "고성능 노트북", 1L);
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findByCategory(eq(1L), any())).thenReturn(productPage); // eq() 사용

        // Act
        Page<ProductInfo.Item> result = productService.getProductsByCategory(1L, PageRequest.of(0, 10));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("노트북");
        assertThat(result.getContent().get(0).price()).isEqualTo(2000);
    }


    @Test
    @DisplayName("단일 상품 상세 조회 테스트")
    void getProductDetail_shouldReturnProductDetail() {
        // Arrange
        Category category = parentCategory;
        Product product = ProductFixture.product("아이폰",BigDecimal.valueOf(1_999_999), "전자기기");
        Stock stock = new Stock(1L, product, 50);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(stockRepository.findById(product.getId())).thenReturn(Optional.of(stock));

        // Act
        ProductInfo.ItemDetail result = productService.getProductDetail(product.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("스마트폰");
        assertThat(result.price()).isEqualTo(1000);
        assertThat(result.quantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("상품이 존재하지 않을 경우 예외 발생 테스트")
    void getProductDetail_shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            productService.getProductDetail(productId);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Product not found");
        }
    }

    @Test
    @DisplayName("재고가 존재하지 않을 경우 예외 발생 테스트")
    void getProductDetail_shouldThrowExceptionWhenStockNotFound() {
        // Arrange
        Category category = parentCategory;
        Product product = new Product(1L, "스마트폰", BigDecimal.valueOf(1_000_000L), "최신 스마트폰", 1L);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(stockRepository.findById(product.getId())).thenReturn(Optional.empty());

        // Act & Assert
        try {
            productService.getProductDetail(product.getId());
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Stock not found");
        }
    }


}