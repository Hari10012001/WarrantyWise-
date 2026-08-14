package com.warrantywise.specification;

import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WarrantySpecification {

    private WarrantySpecification() {
        // Utility class
    }

    public static Specification<Warranty> filter(
            Long userId,
            Long productId,
            WarrantyType warrantyType,
            WarrantyStatus status,
            String search,
            LocalDate startDate,
            LocalDate endDate,
            Boolean expiringSoon) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("user").get("id"), userId));
            }

            if (productId != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("id"), productId));
            }

            if (warrantyType != null) {
                predicates.add(criteriaBuilder.equal(root.get("warrantyType"), warrantyType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate providerLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("provider")), searchPattern);
                Predicate coverageLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("coverageDetails")), searchPattern);
                Predicate termsLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("termsAndConditions")), searchPattern);
                Predicate productNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("product").get("name")), searchPattern);
                Predicate modelNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("product").get("modelName")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        providerLike,
                        coverageLike,
                        termsLike,
                        productNameLike,
                        modelNameLike
                ));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            if (Boolean.TRUE.equals(expiringSoon)) {
                LocalDate now = LocalDate.now();
                LocalDate thirtyDaysLater = now.plusDays(30);

                Predicate statusExpiringSoon = criteriaBuilder.equal(root.get("status"), WarrantyStatus.EXPIRING_SOON);

                Predicate statusActive = criteriaBuilder.equal(root.get("status"), WarrantyStatus.ACTIVE);
                Predicate endDateBetween = criteriaBuilder.between(root.get("endDate"), now, thirtyDaysLater);
                Predicate activeAndExpiring = criteriaBuilder.and(statusActive, endDateBetween);

                predicates.add(criteriaBuilder.or(statusExpiringSoon, activeAndExpiring));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
