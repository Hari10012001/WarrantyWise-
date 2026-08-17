package com.warrantywise.service.impl;

import com.warrantywise.dto.warranty.WarrantyHealthResponse;
import com.warrantywise.dto.warranty.WarrantySummaryResponse;
import com.warrantywise.dto.warranty.WarrantyTimelineResponse;
import com.warrantywise.entity.Product;
import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.WarrantyRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.WarrantyIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WarrantyIntelligenceServiceImpl implements WarrantyIntelligenceService {

    private final WarrantyRepository warrantyRepository;
    private final ProductRepository productRepository;

    @Override
    public WarrantySummaryResponse getWarrantySummary(UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        long userId = currentUser.getId();

        long totalWarranties;
        long activeCount;
        long expiringSoonCount;
        long expiredCount;
        long extendedCount;
        long totalProducts;
        List<Product> products;

        if (isAdmin) {
            totalWarranties = warrantyRepository.count();
            activeCount = warrantyRepository.countByUserIdAndStatus(userId, WarrantyStatus.ACTIVE);
            expiringSoonCount = warrantyRepository.countByUserIdAndStatus(userId, WarrantyStatus.EXPIRING_SOON);
            expiredCount = warrantyRepository.countByUserIdAndStatus(userId, WarrantyStatus.EXPIRED);
            extendedCount = warrantyRepository.countByUserIdAndWarrantyType(userId, WarrantyType.EXTENDED);
            totalProducts = productRepository.count();
            products = productRepository.findAll();
        } else {
            totalWarranties = warrantyRepository.countByUserId(userId);
            activeCount = warrantyRepository.countByUserIdAndStatus(userId, WarrantyStatus.ACTIVE);
            expiringSoonCount = warrantyRepository.countByUserIdAndStatus(userId, WarrantyStatus.EXPIRING_SOON);
            expiredCount = warrantyRepository.countByUserIdAndStatus(userId, WarrantyStatus.EXPIRED);
            extendedCount = warrantyRepository.countByUserIdAndWarrantyType(userId, WarrantyType.EXTENDED);
            totalProducts = productRepository.countByUserIdAndIsActive(userId, true);
            products = productRepository.findByUserIdAndIsActiveTrue(userId);
        }

        long productsWithActiveWarranty = products.stream()
                .filter(p -> warrantyRepository.findByProductId(p.getId()).stream().anyMatch(w -> w.getStatus() == WarrantyStatus.ACTIVE || w.getStatus() == WarrantyStatus.EXPIRING_SOON))
                .count();

        double warrantyCoveragePercentage = totalProducts > 0 
                ? ((double) productsWithActiveWarranty / totalProducts) * 100.0 
                : 0.0;

        double overallHealthScore = computeHealthScore(activeCount, expiringSoonCount, expiredCount, totalWarranties);
        String healthRating = determineHealthRating(overallHealthScore);

        return WarrantySummaryResponse.builder()
                .totalWarranties(totalWarranties)
                .activeCount(activeCount)
                .expiringSoonCount(expiringSoonCount)
                .expiredCount(expiredCount)
                .extendedCount(extendedCount)
                .totalProducts(totalProducts)
                .productsWithActiveWarranty(productsWithActiveWarranty)
                .warrantyCoveragePercentage(warrantyCoveragePercentage)
                .overallHealthScore(overallHealthScore)
                .healthRating(healthRating)
                .build();
    }

    @Override
    public WarrantyHealthResponse getProductWarrantyHealth(Long productId, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<Warranty> warranties = warrantyRepository.findByProductId(productId);

        long activeCount = warranties.stream().filter(w -> w.getStatus() == WarrantyStatus.ACTIVE).count();
        long expiringSoonCount = warranties.stream().filter(w -> w.getStatus() == WarrantyStatus.EXPIRING_SOON).count();
        long expiredCount = warranties.stream().filter(w -> w.getStatus() == WarrantyStatus.EXPIRED).count();
        long totalWarranties = warranties.size();

        double healthScore = computeHealthScore(activeCount, expiringSoonCount, expiredCount, totalWarranties);
        String healthStatus = determineHealthRating(healthScore);

        List<String> recommendations = generateRecommendations(activeCount, expiringSoonCount, expiredCount);

        return WarrantyHealthResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .healthScore(healthScore)
                .healthStatus(healthStatus)
                .activeWarranties(activeCount)
                .expiringSoonWarranties(expiringSoonCount)
                .expiredWarranties(expiredCount)
                .recommendations(recommendations)
                .build();
    }

    @Override
    public List<WarrantyHealthResponse> getUserProductsWarrantyHealth(UserPrincipal currentUser) {
        List<Product> products = productRepository.findByUserIdAndIsActiveTrue(currentUser.getId());
        
        return products.stream()
                .map(product -> getProductWarrantyHealth(product.getId(), currentUser))
                .sorted(Comparator.comparingDouble(WarrantyHealthResponse::getHealthScore))
                .collect(Collectors.toList());
    }

    @Override
    public List<WarrantyTimelineResponse> getWarrantyTimeline(UserPrincipal currentUser) {
        List<Warranty> warranties = warrantyRepository.findByProductUserId(currentUser.getId());
        
        return warranties.stream()
                .map(this::mapToTimelineResponse)
                .sorted(Comparator.comparing(WarrantyTimelineResponse::getEndDate))
                .collect(Collectors.toList());
    }

    private double computeHealthScore(long activeCount, long expiringSoonCount, long expiredCount, long total) {
        if (total == 0) return 0.0;
        return (activeCount * 100.0 + expiringSoonCount * 50.0 + expiredCount * 0.0) / total;
    }

    private String determineHealthRating(double score) {
        if (score > 85) return "EXCELLENT";
        if (score > 70) return "GOOD";
        if (score > 40) return "WARNING";
        return "CRITICAL";
    }

    private List<String> generateRecommendations(long active, long expiringSoon, long expired) {
        List<String> recommendations = new ArrayList<>();
        if (active == 0 && expiringSoon == 0) {
            recommendations.add("No active warranty coverage. Consider purchasing an extended warranty.");
        }
        if (expiringSoon > 0) {
            recommendations.add("Warranty expiring soon. Request renewal or extension to maintain coverage.");
        }
        if (expired > 0) {
            recommendations.add("Some warranties have expired. Check if extensions are still possible.");
        }
        if (active > 0) {
            recommendations.add("Coverage is active. Keep purchase receipts and documents handy.");
        }
        return recommendations;
    }

    private WarrantyTimelineResponse mapToTimelineResponse(Warranty warranty) {
        long daysRemaining = 0;
        if (warranty.getEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), warranty.getEndDate());
        }
        
        String milestone;
        if (daysRemaining <= 0 || warranty.getStatus() == WarrantyStatus.EXPIRED) {
            milestone = "EXPIRED";
        } else if (daysRemaining <= 15) {
            milestone = "15_DAYS_REMAINING";
        } else if (daysRemaining <= 30) {
            milestone = "30_DAYS_REMAINING";
        } else {
            milestone = "START";
        }

        return WarrantyTimelineResponse.builder()
                .warrantyId(warranty.getId())
                .productId(warranty.getProduct() != null ? warranty.getProduct().getId() : null)
                .productName(warranty.getProduct() != null ? warranty.getProduct().getName() : null)
                .warrantyType(warranty.getWarrantyType())
                .provider(warranty.getProvider())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .status(warranty.getStatus())
                .daysRemaining(daysRemaining)
                .milestone(milestone)
                .build();
    }
}
