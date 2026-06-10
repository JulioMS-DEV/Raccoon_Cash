package com.raccooncash.api.category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByActiveTrue();
    List<Category> findAllByTypeAndActiveTrue(CategoryType type);
    Optional<Category> findByIdAndActiveTrue(Long id);
    boolean existsByNameIgnoreCaseAndTypeAndActiveTrue(String name, CategoryType type);
}
