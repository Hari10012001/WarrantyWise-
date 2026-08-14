package com.warrantywise.repository;

import com.warrantywise.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueOrderByNameAsc();

    Optional<Category> findByNameIgnoreCase(String name);

    Boolean existsByNameIgnoreCase(String name);

    long countByIsActive(Boolean isActive);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Category> findByNameContainingIgnoreCaseAndIsActive(String name, Boolean isActive, Pageable pageable);

    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);
}
