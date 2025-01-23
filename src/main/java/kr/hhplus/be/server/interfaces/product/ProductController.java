package kr.hhplus.be.server.interfaces.product;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.product.ProductInfo;
import kr.hhplus.be.server.application.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "상품 API")
public class ProductController {

    private final ProductService productService;

//    @GetMapping
//    public List<ProductResponse> getAllProducts() {
//        return List.of(
//                new ProductResponse(1L, "폴로 가디건", "clothing", 90000, 20),
//                new ProductResponse(2L, "뉴발란스 백팩", "bag", 70000, 30),
//                new ProductResponse(3L, "어그 부츠", "shoes", 180000, 50),
//                new ProductResponse(4L, "스탠리 텀블러", "lifestyle", 180000, 10)
//        );
//    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {

        if (categoryId == null) {
            Page<ProductInfo.Item> allProducts = productService.getAllProducts(pageable);
            return ResponseEntity.ok(allProducts.map(ProductResponse::from));
        }

        Page<ProductInfo.Item> productsByCategory = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(productsByCategory.map(ProductResponse::from));
    }

//    @GetMapping("/popular")
//    public List<ProductResponse> getPopularProducts() {
//        return List.of(
//                new ProductResponse(3L, "어그 부츠", "shoes", 180000, 50),
//                new ProductResponse(1L, "폴로 가디건", "clothing", 90000, 20)
//        );
//    }

}
