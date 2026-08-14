package com.warrantywise.repository;

import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long>, JpaSpecificationExecutor<Warranty> {

    List<Warranty> findByProductId(Long productId);

    long countByProductId(Long productId);

    boolean existsByProductIdAndWarrantyTypeAndStatusNot(Long productId, WarrantyType warrantyType, WarrantyStatus status);

    List<Warranty> findByProductIdOrderByEndDateAsc(Long productId);

    @Query("SELECT w FROM Warranty w JOIN w.product p WHERE p.user.id = :userId")
    Page<Warranty> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT w FROM Warranty w JOIN w.product p WHERE p.user.id = :userId AND w.status = :status")
    Page<Warranty> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") WarrantyStatus status, Pageable pageable);

    @Query("SELECT w FROM Warranty w WHERE w.status = 'ACTIVE' AND w.endDate <= :date")
    List<Warranty> findExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT w FROM Warranty w WHERE w.status != 'EXPIRED' AND w.endDate < CURRENT_DATE")
    List<Warranty> findNewlyExpired();

    @Query("SELECT COUNT(w) FROM Warranty w JOIN w.product p WHERE p.user.id = :userId AND w.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") WarrantyStatus status);

    @Query("SELECT COUNT(w) FROM Warranty w JOIN w.product p WHERE p.user.id = :userId AND w.warrantyType = :warrantyType")
    long countByUserIdAndWarrantyType(@Param("userId") Long userId, @Param("warrantyType") WarrantyType warrantyType);

    @Query("SELECT w FROM Warranty w JOIN FETCH w.product p WHERE p.user.id = :userId")
    List<Warranty> findByProductUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM Warranty w JOIN FETCH w.product p JOIN FETCH p.user WHERE w.endDate BETWEEN :startDate AND :endDate AND w.status = 'ACTIVE'")
    List<Warranty> findExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT w FROM Warranty w JOIN FETCH w.product p WHERE w.id = :id AND p.user.id = :userId")
    Optional<Warranty> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT(w) FROM Warranty w JOIN w.product p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}

