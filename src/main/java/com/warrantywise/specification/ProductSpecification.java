package com.warrantywise.specification;

import com.warrantywise.entity.Product;
import com.warrantywise.entity.Tag;
import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.ProductCondition;
import com.warrantywise.enums.ProductStatus;
import com.warrantywise.enums.PurchaseMode;
import com.warrantywise.enums.WarrantyStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {
        // Utility class
    }

    public static Specification<Product> filter(
            Long userId,
            Long categoryId,
            Long brandId,
            ProductStatus status,
            ProductCondition condition,
            PurchaseMode purchaseMode,
            WarrantyStatus warrantyStatus,
            Boolean isActive,
            String search,
            LocalDate startDate,
            LocalDate endDate,
            Long tagId,
            String storageLocation) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("productStatus"), status));
            }

            if (condition != null) {
                predicates.add(criteriaBuilder.equal(root.get("productCondition"), condition));
            }

            if (purchaseMode != null) {
                predicates.add(criteriaBuilder.equal(root.get("purchaseMode"), purchaseMode));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            if (storageLocation != null && !storageLocation.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("storageLocation")), "%" + storageLocation.trim().toLowerCase() + "%"));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate modelNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("modelName")), searchPattern);
                Predicate modelNumberLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("modelNumber")), searchPattern);
                Predicate serialNumberLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("serialNumber")), searchPattern);
                Predicate imeiNumberLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("imeiNumber")), searchPattern);
                Predicate barcodeLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("barcode")), searchPattern);
                Predicate invoiceNumberLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("invoiceNumber")), searchPattern);
                Predicate retailerLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("retailer")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        nameLike,
                        modelNameLike,
                        modelNumberLike,
                        serialNumberLike,
                        imeiNumberLike,
                        barcodeLike,
                        invoiceNumberLike,
                        retailerLike
                ));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("purchaseDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("purchaseDate"), endDate));
            }

            if (tagId != null) {
                Join<Product, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(tagJoin.get("id"), tagId));
                query.distinct(true);
            }

            if (warrantyStatus != null) {
                Join<Product, Warranty> warrantyJoin = root.join("warranties", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(warrantyJoin.get("status"), warrantyStatus));
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
