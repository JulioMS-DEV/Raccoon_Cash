package com.raccooncash.api.category;
import com.raccooncash.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(CategoryType type) {
        List<Category> categories = type == null
                ? categoryRepository.findAllByActiveTrue()
                : categoryRepository.findAllByTypeAndActiveTrue(type);
        return categories.stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findActiveCategory(id);
        return new CategoryResponse(category);
    }
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());
        category.setActive(true);
        Category savedCategory = categoryRepository.save(category);
        return new CategoryResponse(savedCategory);
    }
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findActiveCategory(id);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());
        Category updatedCategory = categoryRepository.save(category);
        return new CategoryResponse(updatedCategory);
    }
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findActiveCategory(id);
        category.setActive(false);
        categoryRepository.save(category);
    }
    private Category findActiveCategory(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
    }
}
