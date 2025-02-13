package kr.hhplus.be.server.infra.product;

import kr.hhplus.be.server.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product,Long> {


    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // 전체 상품 목록 조회 (페이징 처리)
    @Query("""
        SELECT p
        FROM Product p
    """)
    Page<Product> findAllProducts(Pageable pageable);



}
