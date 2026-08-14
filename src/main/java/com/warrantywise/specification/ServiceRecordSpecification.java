package com.warrantywise.specification;

import com.warrantywise.entity.ServiceRecord;
import com.warrantywise.enums.ServiceType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceRecordSpecification {

    private ServiceRecordSpecification() {
        // Utility class
    }

    public static Specification<ServiceRecord> filter(
            Long userId,
            Long productId,
            ServiceType serviceType,
            String serviceProvider,
            String serviceStatus,
            String search,
            LocalDate startDate,
            LocalDate endDate,
            Boolean upcomingOnly) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("user").get("id"), userId));
            }

            if (productId != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("id"), productId));
            }

            if (serviceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceType"), serviceType));
            }

            if (serviceProvider != null && !serviceProvider.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("serviceProvider")),
                        "%" + serviceProvider.trim().toLowerCase() + "%"
                ));
            }

            if (serviceStatus != null && !serviceStatus.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("serviceStatus")),
                        serviceStatus.trim().toLowerCase()
                ));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("serviceDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("serviceDate"), endDate));
            }

            if (Boolean.TRUE.equals(upcomingOnly)) {
                predicates.add(criteriaBuilder.isNotNull(root.get("nextServiceDate")));
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("nextServiceDate"), LocalDate.now()));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate providerLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("serviceProvider")), searchPattern);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate workLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("workPerformed")), searchPattern);
                Predicate partsLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("partsReplaced")), searchPattern);
                Predicate notesLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), searchPattern);
                Predicate productNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("product").get("name")), searchPattern);
                Predicate modelNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("product").get("modelName")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        providerLike,
                        descLike,
                        workLike,
                        partsLike,
                        notesLike,
                        productNameLike,
                        modelNameLike
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
