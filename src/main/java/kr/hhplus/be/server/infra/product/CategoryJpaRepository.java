package kr.hhplus.be.server.infra.product;

import kr.hhplus.be.server.domain.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

}
