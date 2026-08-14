package com.warrantywise.service.impl;

import com.warrantywise.dto.warranty.WarrantyRequest;
import com.warrantywise.dto.warranty.WarrantyResponse;
import com.warrantywise.entity.ActivityLog;
import com.warrantywise.entity.Product;
import com.warrantywise.entity.User;
import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.ActionType;
import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import com.warrantywise.exception.BadRequestException;
import com.warrantywise.exception.DuplicateResourceException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.exception.UnauthorizedException;
import com.warrantywise.mapper.WarrantyMapper;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.repository.AttachmentRepository;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.UserRepository;
import com.warrantywise.repository.WarrantyRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.WarrantyService;
import com.warrantywise.specification.WarrantySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final ProductRepository productRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final WarrantyMapper warrantyMapper;

    @Override
    public WarrantyResponse createWarranty(WarrantyRequest request, String ipAddress, UserPrincipal currentUser) {
        Product product = getProductAndCheckAuth(request.getProductId(), currentUser);
        validateWarrantyDates(request, product);
        checkManufacturerWarrantyDuplication(product.getId(), request.getWarrantyType());

        Warranty warranty = warrantyMapper.toEntity(request, product);
        warranty.setStatus(calculateWarrantyStatus(request.getStartDate(), request.getEndDate()));

        Warranty savedWarranty = warrantyRepository.save(warranty);
        logActivity(ActionType.CREATE, "Created warranty for product: " + product.getName(), ipAddress, currentUser, savedWarranty.getId());

        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("WARRANTY", savedWarranty.getId());
        return warrantyMapper.toResponse(savedWarranty, attachmentCount);
    }

    @Override
    public WarrantyResponse updateWarranty(Long id, WarrantyRequest request, String ipAddress, UserPrincipal currentUser) {
        Warranty warranty = getWarrantyAndCheckAuth(id, currentUser);
        Product product = warranty.getProduct();

        validateWarrantyDates(request, product);
        
        if (request.getWarrantyType() != warranty.getWarrantyType() && request.getWarrantyType() == WarrantyType.MANUFACTURER) {
            checkManufacturerWarrantyDuplication(product.getId(), request.getWarrantyType());
        }

        warrantyMapper.updateEntityFromRequest(request, warranty, product);
        warranty.setStatus(calculateWarrantyStatus(request.getStartDate(), request.getEndDate()));

        Warranty updatedWarranty = warrantyRepository.save(warranty);
        logActivity(ActionType.UPDATE, "Updated warranty for product: " + product.getName(), ipAddress, currentUser, updatedWarranty.getId());

        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("WARRANTY", updatedWarranty.getId());
        return warrantyMapper.toResponse(updatedWarranty, attachmentCount);
    }

    @Override
    public void deleteWarranty(Long id, String ipAddress, UserPrincipal currentUser) {
        Warranty warranty = getWarrantyAndCheckAuth(id, currentUser);
        warrantyRepository.delete(warranty);
        logActivity(ActionType.DELETE, "Deleted warranty for product: " + warranty.getProduct().getName(), ipAddress, currentUser, id);
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyResponse getWarrantyById(Long id, UserPrincipal currentUser) {
        Warranty warranty = getWarrantyAndCheckAuth(id, currentUser);
        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("WARRANTY", id);
        return warrantyMapper.toResponse(warranty, attachmentCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyResponse> getWarrantiesByProduct(Long productId, UserPrincipal currentUser) {
        getProductAndCheckAuth(productId, currentUser);
        List<Warranty> warranties = warrantyRepository.findByProductId(productId);
        return warranties.stream()
                .map(w -> warrantyMapper.toResponse(w, attachmentRepository.countByEntityTypeAndEntityId("WARRANTY", w.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarrantyResponse> getActiveWarranties(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        return searchWarranties(null, null, WarrantyStatus.ACTIVE, null, null, null, null, page, size, sortBy, sortDir, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarrantyResponse> getExpiredWarranties(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        return searchWarranties(null, null, WarrantyStatus.EXPIRED, null, null, null, null, page, size, sortBy, sortDir, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarrantyResponse> getExpiringSoonWarranties(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        return searchWarranties(null, null, WarrantyStatus.EXPIRING_SOON, null, null, null, true, page, size, sortBy, sortDir, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarrantyResponse> searchWarranties(Long productId, WarrantyType warrantyType, WarrantyStatus status, String search, LocalDate startDate, LocalDate endDate, Boolean expiringSoon, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Long targetUserId = isAdmin(currentUser) ? null : currentUser.getId();

        Specification<Warranty> spec = WarrantySpecification.filter(targetUserId, productId, warrantyType, status, search, startDate, endDate, expiringSoon);
        Page<Warranty> warrantyPage = warrantyRepository.findAll(spec, pageable);

        return warrantyPage.map(w -> warrantyMapper.toResponse(w, attachmentRepository.countByEntityTypeAndEntityId("WARRANTY", w.getId())));
    }

    // Helper Methods

    private Product getProductAndCheckAuth(Long productId, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        if (!isAdmin(currentUser) && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to access this product's warranties");
        }
        return product;
    }

    private Warranty getWarrantyAndCheckAuth(Long warrantyId, UserPrincipal currentUser) {
        Warranty warranty = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty", "id", warrantyId));
                
        if (!isAdmin(currentUser) && !warranty.getProduct().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to access this warranty");
        }
        return warranty;
    }

    private void validateWarrantyDates(WarrantyRequest request, Product product) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("Warranty end date must be after start date");
        }
        if (product.getPurchaseDate() != null && request.getStartDate().isBefore(product.getPurchaseDate())) {
            throw new BadRequestException("Warranty start date cannot be before product purchase date (" + product.getPurchaseDate() + ")");
        }
    }

    private void checkManufacturerWarrantyDuplication(Long productId, WarrantyType warrantyType) {
        if (warrantyType == WarrantyType.MANUFACTURER) {
            boolean exists = warrantyRepository.existsByProductIdAndWarrantyTypeAndStatusNot(
                    productId, WarrantyType.MANUFACTURER, WarrantyStatus.EXPIRED);
            if (exists) {
                throw new DuplicateResourceException("An active or expiring manufacturer warranty already exists for this product");
            }
        }
    }

    private WarrantyStatus calculateWarrantyStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (endDate.isBefore(today) || endDate.isEqual(today)) {
            return WarrantyStatus.EXPIRED;
        } else if (!endDate.isAfter(today.plusDays(30))) {
            return WarrantyStatus.EXPIRING_SOON;
        } else {
            return WarrantyStatus.ACTIVE;
        }
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void logActivity(ActionType actionType, String description, String ipAddress, UserPrincipal currentUser, Long entityId) {
        try {
            User user = userRepository.findById(currentUser.getId()).orElse(null);
            if (user != null) {
                ActivityLog log = ActivityLog.builder()
                        .user(user)
                        .action(actionType)
                        .entityType("WARRANTY")
                        .entityId(entityId)
                        .description(description)
                        .ipAddress(ipAddress)
                        .build();
                activityLogRepository.save(log);
            }
        } catch (Exception e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
}
