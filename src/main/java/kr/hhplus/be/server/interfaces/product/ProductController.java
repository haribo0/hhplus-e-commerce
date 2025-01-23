package kr.hhplus.be.server.interfaces.product;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "상품 API")
public class ProductController {

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return List.of(
                new ProductResponse(1L, "폴로 가디건", "clothing", 90000, 20),
                new ProductResponse(2L, "뉴발란스 백팩", "bag", 70000, 30),
                new ProductResponse(3L, "어그 부츠", "shoes", 180000, 50),
                new ProductResponse(4L, "스탠리 텀블러", "lifestyle", 180000, 10)
        );
    }

    @GetMapping("/popular")
    public List<ProductResponse> getPopularProducts() {
        return List.of(
                new ProductResponse(3L, "어그 부츠", "shoes", 180000, 50),
                new ProductResponse(1L, "폴로 가디건", "clothing", 90000, 20)
        );
    }

}
