package com.ai.main.service;

import com.ai.main.domain.Category;
import com.ai.main.dto.CategoryCreateRequest;
import com.ai.main.dto.CategoryResponse;
import com.ai.main.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllWithProductCount().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다.");
        }

        Category category = Category.builder()
                .name(request.name())
                .build();

        categoryRepository.save(category);
        return new CategoryResponse(category.getId(), category.getName(), 0);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        // Category에 update 메서드가 없으므로 직접 수정을 위해 새 엔티티 방식 대신
        // 간단히 처리 — Category에 name setter 추가 필요
        // 여기서는 JPQL update나 새 save를 쓰지 않고 도메인 메서드 추가
        category.updateName(request.name());
        return new CategoryResponse(category.getId(), category.getName(),
                category.getProducts() != null ? category.getProducts().size() : 0);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("카테고리를 찾을 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }
}