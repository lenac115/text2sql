package com.ai.main.repository;

import com.ai.main.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products ORDER BY c.name")
    List<Category> findAllWithProductCount();
}