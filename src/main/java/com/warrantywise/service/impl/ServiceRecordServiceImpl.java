package com.warrantywise.service.impl;

import com.warrantywise.dto.servicerecord.ServiceRecordRequest;
import com.warrantywise.dto.servicerecord.ServiceRecordResponse;
import com.warrantywise.entity.ActivityLog;
import com.warrantywise.entity.Product;
import com.warrantywise.entity.ServiceRecord;
import com.warrantywise.entity.User;
import com.warrantywise.enums.ActionType;
import com.warrantywise.enums.ServiceType;
import com.warrantywise.exception.BadRequestException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.exception.UnauthorizedException;
import com.warrantywise.mapper.ServiceRecordMapper;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.repository.AttachmentRepository;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.ServiceRecordRepository;
import com.warrantywise.repository.UserRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ServiceRecordService;
import com.warrantywise.specification.ServiceRecordSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final ProductRepository productRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ServiceRecordMapper serviceRecordMapper;

    private void validateServiceRecordDatesAndCost(Product product, ServiceRecordRequest request) {
        if (product.getPurchaseDate() != null && request.getServiceDate().isBefore(product.getPurchaseDate())) {
            throw new BadRequestException("Service date cannot be before product purchase date (" + product.getPurchaseDate() + ")");
        }
        if (request.getCompletionDate() != null && request.getCompletionDate().isBefore(request.getServiceDate())) {
            throw new BadRequestException("Service completion date cannot be before service date");
        }
        if (request.getNextServiceDate() != null && !request.getNextServiceDate().isAfter(request.getServiceDate())) {
            throw new BadRequestException("Next service date must be after service date");
        }
        if (request.getCost() != null && request.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Service cost cannot be negative");
        }
    }

    private void checkOwnership(Product product, UserPrincipal currentUser) {
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) &&
            !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to access this product's service records");
        }
    }

    private void logActivity(ActionType actionType, String description, String ipAddress, UserPrincipal currentUser, Long entityId) {
        try {
            User user = userRepository.findById(currentUser.getId()).orElse(null);
            if (user != null) {
                ActivityLog log = ActivityLog.builder()
                        .user(user)
                        .action(actionType)
                        .entityType("SERVICE_RECORD")
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

    @Override
    public ServiceRecordResponse createServiceRecord(ServiceRecordRequest request, String ipAddress, UserPrincipal currentUser) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        checkOwnership(product, currentUser);
        validateServiceRecordDatesAndCost(product, request);

        ServiceRecord serviceRecord = serviceRecordMapper.toEntity(request, product);
        serviceRecord = serviceRecordRepository.save(serviceRecord);

        logActivity(ActionType.CREATE, "Created service record for product: " + product.getName(), ipAddress, currentUser, serviceRecord.getId());

        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("SERVICE_RECORD", serviceRecord.getId());
        return serviceRecordMapper.toResponse(serviceRecord, attachmentCount);
    }

    @Override
    public ServiceRecordResponse updateServiceRecord(Long id, ServiceRecordRequest request, String ipAddress, UserPrincipal currentUser) {
        ServiceRecord serviceRecord = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Record not found with id: " + id));

        Product product = serviceRecord.getProduct();
        checkOwnership(product, currentUser);

        if (!product.getId().equals(request.getProductId())) {
             throw new BadRequestException("Cannot change the product of an existing service record");
        }

        validateServiceRecordDatesAndCost(product, request);

        serviceRecordMapper.updateEntityFromRequest(request, serviceRecord, product);
        serviceRecord = serviceRecordRepository.save(serviceRecord);

        logActivity(ActionType.UPDATE, "Updated service record for product: " + product.getName(), ipAddress, currentUser, serviceRecord.getId());

        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("SERVICE_RECORD", id);
        return serviceRecordMapper.toResponse(serviceRecord, attachmentCount);
    }

    @Override
    public void deleteServiceRecord(Long id, String ipAddress, UserPrincipal currentUser) {
        ServiceRecord serviceRecord = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Record not found with id: " + id));

        Product product = serviceRecord.getProduct();
        checkOwnership(product, currentUser);

        serviceRecordRepository.delete(serviceRecord);

        logActivity(ActionType.DELETE, "Deleted service record for product: " + product.getName(), ipAddress, currentUser, id);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRecordResponse getServiceRecordById(Long id, UserPrincipal currentUser) {
        ServiceRecord serviceRecord = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Record not found with id: " + id));

        checkOwnership(serviceRecord.getProduct(), currentUser);

        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("SERVICE_RECORD", id);
        return serviceRecordMapper.toResponse(serviceRecord, attachmentCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRecordResponse> getServiceRecordsByProduct(Long productId, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        checkOwnership(product, currentUser);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ServiceRecord> serviceRecords = serviceRecordRepository.findByProductId(productId, pageable);
        return serviceRecords.map(record -> {
            long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("SERVICE_RECORD", record.getId());
            return serviceRecordMapper.toResponse(record, attachmentCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRecordResponse> getUserServiceRecords(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        return searchServiceRecords(null, null, null, null, null, null, null, null, page, size, sortBy, sortDir, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceRecordResponse> searchServiceRecords(Long productId, ServiceType serviceType, String serviceProvider, String serviceStatus, String search, LocalDate startDate, LocalDate endDate, Boolean upcomingOnly, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long userId = isAdmin ? null : currentUser.getId();

        Specification<ServiceRecord> spec = ServiceRecordSpecification.filter(userId, productId, serviceType, serviceProvider, serviceStatus, search, startDate, endDate, upcomingOnly);
        Page<ServiceRecord> serviceRecords = serviceRecordRepository.findAll(spec, pageable);

        return serviceRecords.map(record -> {
            long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("SERVICE_RECORD", record.getId());
            return serviceRecordMapper.toResponse(record, attachmentCount);
        });
    }
}
