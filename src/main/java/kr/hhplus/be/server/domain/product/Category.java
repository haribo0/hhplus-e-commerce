package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> child = new ArrayList<>();

    @Builder
    public Category(String name, String description, Boolean isActive, Category parent) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.parent = parent;
    }

    // 자식 카테고리 추가
    public void addChildCategory(Category childCategory) {
        childCategory.parent = this;
        this.child.add(childCategory);
    }

}

