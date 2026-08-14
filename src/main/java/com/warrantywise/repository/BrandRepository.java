package com.warrantywise.repository;

import com.warrantywise.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findByIsActiveTrueOrderByNameAsc();

    Optional<Brand> findByNameIgnoreCase(String name);

    Boolean existsByNameIgnoreCase(String name);

    long countByIsActive(Boolean isActive);

    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Brand> findByNameContainingIgnoreCaseAndIsActive(String name, Boolean isActive, Pageable pageable);

    Page<Brand> findByIsActive(Boolean isActive, Pageable pageable);
}
