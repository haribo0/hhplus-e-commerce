package kr.hhplus.be.server.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Page<Product> findByCategory(Long categoryId, Pageable pageable);

    Page<Product> findAllProducts(Pageable pageable);

    Optional<Product> findById(Long id);

}
