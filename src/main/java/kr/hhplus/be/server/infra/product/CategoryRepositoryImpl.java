package kr.hhplus.be.server.infra.product;

import kr.hhplus.be.server.domain.product.Category;
import kr.hhplus.be.server.domain.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }
}
