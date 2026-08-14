package com.warrantywise.repository;

import com.warrantywise.entity.ServiceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long>, JpaSpecificationExecutor<ServiceRecord> {

    @Query("SELECT sr FROM ServiceRecord sr JOIN FETCH sr.product p WHERE sr.id = :id AND p.user.id = :userId")
    Optional<ServiceRecord> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT(sr) FROM ServiceRecord sr JOIN sr.product p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    List<ServiceRecord> findByProductId(Long productId);

    Page<ServiceRecord> findByProductId(Long productId, Pageable pageable);

    @Query("SELECT sr FROM ServiceRecord sr JOIN FETCH sr.product p WHERE p.user.id = :userId")
    Page<ServiceRecord> findByProductUserId(@Param("userId") Long userId, Pageable pageable);

    long countByProductId(Long productId);

    List<ServiceRecord> findByProductIdOrderByServiceDateDesc(Long productId);

    @Query("SELECT COALESCE(SUM(sr.cost), 0) FROM ServiceRecord sr JOIN sr.product p WHERE (:userId IS NULL OR p.user.id = :userId)")
    BigDecimal sumTotalCost(@Param("userId") Long userId);

    @Query("SELECT COALESCE(AVG(sr.cost), 0) FROM ServiceRecord sr JOIN sr.product p WHERE (:userId IS NULL OR p.user.id = :userId)")
    Double avgCost(@Param("userId") Long userId);

    @Query("SELECT COUNT(sr) FROM ServiceRecord sr JOIN sr.product p WHERE (:userId IS NULL OR p.user.id = :userId) AND sr.serviceDate BETWEEN :startDate AND :endDate")
    long countServicesBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(sr.cost), 0) FROM ServiceRecord sr JOIN sr.product p WHERE (:userId IS NULL OR p.user.id = :userId) AND sr.serviceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCostBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p.name, SUM(sr.cost), COUNT(sr) FROM ServiceRecord sr JOIN sr.product p WHERE (:userId IS NULL OR p.user.id = :userId) GROUP BY p.id, p.name ORDER BY SUM(sr.cost) DESC")
    List<Object[]> findServiceCostByProduct(@Param("userId") Long userId);

    @Query("SELECT c.name, SUM(sr.cost), COUNT(sr) FROM ServiceRecord sr JOIN sr.product p JOIN p.category c WHERE (:userId IS NULL OR p.user.id = :userId) GROUP BY c.id, c.name ORDER BY SUM(sr.cost) DESC")
    List<Object[]> findServiceCostByCategory(@Param("userId") Long userId);

    @Query("SELECT sr.serviceType, SUM(sr.cost), COUNT(sr) FROM ServiceRecord sr JOIN sr.product p WHERE (:userId IS NULL OR p.user.id = :userId) GROUP BY sr.serviceType ORDER BY SUM(sr.cost) DESC")
    List<Object[]> findServiceCostByServiceType(@Param("userId") Long userId);

    @Query("SELECT YEAR(sr.serviceDate), MONTH(sr.serviceDate), SUM(sr.cost), COUNT(sr) FROM ServiceRecord sr JOIN sr.product p WHERE (:userId IS NULL OR p.user.id = :userId) GROUP BY YEAR(sr.serviceDate), MONTH(sr.serviceDate) ORDER BY YEAR(sr.serviceDate) ASC, MONTH(sr.serviceDate) ASC")
    List<Object[]> findMonthlyTrend(@Param("userId") Long userId);

    @Query("SELECT sr FROM ServiceRecord sr JOIN FETCH sr.product p WHERE (:userId IS NULL OR p.user.id = :userId) AND sr.nextServiceDate IS NOT NULL AND sr.nextServiceDate < CURRENT_DATE")
    List<ServiceRecord> findOverdueServicesByUser(@Param("userId") Long userId);

    @Query("SELECT sr FROM ServiceRecord sr JOIN FETCH sr.product p WHERE (:userId IS NULL OR p.user.id = :userId) AND sr.nextServiceDate IS NOT NULL AND sr.nextServiceDate >= CURRENT_DATE")
    List<ServiceRecord> findUpcomingServicesByUser(@Param("userId") Long userId);

    @Query("SELECT sr FROM ServiceRecord sr JOIN sr.product p WHERE p.user.id = :userId ORDER BY sr.serviceDate DESC")
    Page<ServiceRecord> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(sr.cost), 0) FROM ServiceRecord sr WHERE sr.product.id = :productId")
    BigDecimal sumCostByProductId(@Param("productId") Long productId);

    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.nextServiceDate IS NOT NULL AND sr.nextServiceDate <= :date")
    List<ServiceRecord> findOverdueServices(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(sr.cost), 0) FROM ServiceRecord sr JOIN sr.product p WHERE p.user.id = :userId")
    BigDecimal sumTotalCostByUserId(@Param("userId") Long userId);

    @Query("SELECT MONTH(sr.serviceDate) as month, YEAR(sr.serviceDate) as year, COALESCE(SUM(sr.cost), 0) as totalCost FROM ServiceRecord sr JOIN sr.product p WHERE p.user.id = :userId AND sr.serviceDate BETWEEN :startDate AND :endDate GROUP BY YEAR(sr.serviceDate), MONTH(sr.serviceDate) ORDER BY YEAR(sr.serviceDate), MONTH(sr.serviceDate)")
    List<Object[]> findMonthlyCostByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
