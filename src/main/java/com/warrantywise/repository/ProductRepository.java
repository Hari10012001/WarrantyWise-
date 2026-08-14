package com.warrantywise.repository;

import com.warrantywise.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByUserId(Long userId, Pageable pageable);

    Page<Product> findByUserIdAndIsActive(Long userId, Boolean isActive, Pageable pageable);

    List<Product> findByUserIdAndIsActiveTrue(Long userId);

    long countByUserIdAndIsActive(Long userId, Boolean isActive);

    long countByUserId(Long userId);

    long countByCategoryId(Long categoryId);

    long countByBrandId(Long brandId);

    List<Product> findTop3ByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand WHERE p.id = :id AND p.user.id = :userId")
    Optional<Product> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT p.retailer FROM Product p WHERE p.user.id = :userId AND p.retailer IS NOT NULL")
    List<String> findDistinctRetailersByUserId(@Param("userId") Long userId);

    @Query("SELECT p.category.id, COUNT(p) FROM Product p GROUP BY p.category.id")
    List<Object[]> countProductsGroupedByCategoryId();

    @Query("SELECT p.brand.id, COUNT(p) FROM Product p WHERE p.brand IS NOT NULL GROUP BY p.brand.id")
    List<Object[]> countProductsGroupedByBrandId();

    @Query("SELECT t.id, COUNT(p) FROM Product p JOIN p.tags t WHERE p.user.id = :userId GROUP BY t.id")
    List<Object[]> countProductsGroupedByTagIdForUser(@Param("userId") Long userId);

    boolean existsBySerialNumberAndUserId(String serialNumber, Long userId);

    boolean existsByImeiNumberAndUserId(String imeiNumber, Long userId);

    Page<Product> findByUserIdAndCategoryIdAndIsActive(Long userId, Long categoryId, Boolean isActive, Pageable pageable);

    Page<Product> findByUserIdAndBrandIdAndIsActive(Long userId, Long brandId, Boolean isActive, Pageable pageable);
}
