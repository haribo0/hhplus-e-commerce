package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    // 상품 리스트 조회
    public Page<ProductInfo.Item> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAllProducts(pageable);
        return products.map(product -> new ProductInfo.Item(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        ));
    }

    // 카테고리별 상품 리스트 조회
    public Page<ProductInfo.Item> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategory(categoryId, pageable);
        return products.map(product -> new ProductInfo.Item(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        ));
    }

    // 단일 상품 조회
    public ProductInfo.ItemDetail getProductDetail(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        return new ProductInfo.ItemDetail(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                stock.getQuantity()
        );
    }


}
