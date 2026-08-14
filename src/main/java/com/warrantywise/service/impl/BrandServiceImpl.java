package com.warrantywise.service.impl;

import com.warrantywise.dto.brand.BrandRequest;
import com.warrantywise.dto.brand.BrandResponse;
import com.warrantywise.exception.DuplicateResourceException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.mapper.BrandMapper;
import com.warrantywise.entity.ActivityLog;
import com.warrantywise.entity.Brand;
import com.warrantywise.entity.User;
import com.warrantywise.enums.ActionType;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.repository.BrandRepository;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.UserRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.BrandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final BrandMapper brandMapper;

    public BrandServiceImpl(BrandRepository brandRepository, ProductRepository productRepository, 
                            ActivityLogRepository activityLogRepository, UserRepository userRepository, 
                            BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.brandMapper = brandMapper;
    }

    private void logActivity(ActionType action, String entityType, Long entityId, String description, UserPrincipal currentUser, String ipAddress) {
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        ActivityLog log = new ActivityLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setUser(user);
        log.setIpAddress(ipAddress);
        activityLogRepository.save(log);
    }

    @Override
    public BrandResponse createBrand(BrandRequest request, String ipAddress, UserPrincipal currentUser) {
        if (brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Brand with name '" + request.getName() + "' already exists");
        }
        
        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);
        
        logActivity(ActionType.CREATE, "BRAND", savedBrand.getId(), "Created brand: " + savedBrand.getName(), currentUser, ipAddress);
        
        return brandMapper.toResponse(savedBrand, 0L);
    }

    @Override
    public BrandResponse updateBrand(Long id, BrandRequest request, String ipAddress, UserPrincipal currentUser) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
                
        if (!brand.getName().equalsIgnoreCase(request.getName()) && brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Brand with name '" + request.getName() + "' already exists");
        }
        
        brandMapper.updateEntityFromRequest(request, brand);
        Brand updated = brandRepository.save(brand);
        
        long productCount = productRepository.countByBrandId(id);
        
        logActivity(ActionType.UPDATE, "BRAND", id, "Updated brand: " + updated.getName(), currentUser, ipAddress);
        
        return brandMapper.toResponse(updated, productCount);
    }

    @Override
    public void deleteBrand(Long id, String ipAddress, UserPrincipal currentUser) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
                
        brand.setIsActive(false);
        brandRepository.save(brand);
        
        logActivity(ActionType.DELETE, "BRAND", id, "Deactivated brand: " + brand.getName(), currentUser, ipAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        long productCount = productRepository.countByBrandId(id);
        return brandMapper.toResponse(brand, productCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllActiveBrands() {
        List<Brand> list = brandRepository.findByIsActiveTrueOrderByNameAsc();
        Map<Long, Long> productCountMap = getProductCountMap();
        return list.stream()
                .map(b -> brandMapper.toResponse(b, productCountMap.getOrDefault(b.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getAllBrands(int page, int size, String sortBy, String sortDir, String search, Boolean isActive) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Brand> brandPage;
        if (search != null && !search.isEmpty()) {
            if (isActive != null) {
                brandPage = brandRepository.findByNameContainingIgnoreCaseAndIsActive(search, isActive, pageable);
            } else {
                brandPage = brandRepository.findByNameContainingIgnoreCase(search, pageable);
            }
        } else {
            if (isActive != null) {
                brandPage = brandRepository.findByIsActive(isActive, pageable);
            } else {
                brandPage = brandRepository.findAll(pageable);
            }
        }
        
        Map<Long, Long> productCountMap = getProductCountMap();
        
        List<BrandResponse> responses = brandPage.getContent().stream()
                .map(b -> brandMapper.toResponse(b, productCountMap.getOrDefault(b.getId(), 0L)))
                .collect(Collectors.toList());
                
        return new PageImpl<>(responses, pageable, brandPage.getTotalElements());
    }

    private Map<Long, Long> getProductCountMap() {
        return productRepository.countProductsGroupedByBrandId().stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }
}
