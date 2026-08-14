package com.warrantywise.service.impl;

import com.warrantywise.dto.category.CategoryRequest;
import com.warrantywise.dto.category.CategoryResponse;
import com.warrantywise.entity.ActivityLog;
import com.warrantywise.entity.Category;
import com.warrantywise.entity.User;
import com.warrantywise.enums.ActionType;
import com.warrantywise.exception.DuplicateResourceException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.mapper.CategoryMapper;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.repository.CategoryRepository;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.UserRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               ActivityLogRepository activityLogRepository,
                               UserRepository userRepository,
                               CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request, String ipAddress, UserPrincipal currentUser) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(request);
        category = categoryRepository.save(category);

        logActivity(ActionType.CREATE, "CATEGORY", category.getId(), "Created category: " + category.getName(), currentUser.getId(), ipAddress);

        return categoryMapper.toResponse(category, 0L);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request, String ipAddress, UserPrincipal currentUser) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getName().equalsIgnoreCase(request.getName()) && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        categoryMapper.updateEntityFromRequest(request, category);
        Category updated = categoryRepository.save(category);

        long productCount = productRepository.countByCategoryId(id);

        logActivity(ActionType.UPDATE, "CATEGORY", id, "Updated category: " + updated.getName(), currentUser.getId(), ipAddress);

        return categoryMapper.toResponse(updated, productCount);
    }

    @Override
    public void deleteCategory(Long id, String ipAddress, UserPrincipal currentUser) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setIsActive(false);
        categoryRepository.save(category);

        logActivity(ActionType.DELETE, "CATEGORY", id, "Deactivated category: " + category.getName(), currentUser.getId(), ipAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        long productCount = productRepository.countByCategoryId(id);

        return categoryMapper.toResponse(category, productCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> list = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        Map<Long, Long> productCountMap = getProductCountMap();

        return list.stream()
                .map(c -> categoryMapper.toResponse(c, productCountMap.getOrDefault(c.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(int page, int size, String sortBy, String sortDir, String search, Boolean isActive) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Category> categoryPage;

        if (search != null && !search.trim().isEmpty()) {
            if (isActive != null) {
                categoryPage = categoryRepository.findByNameContainingIgnoreCaseAndIsActive(search.trim(), isActive, pageable);
            } else {
                categoryPage = categoryRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
            }
        } else {
            if (isActive != null) {
                categoryPage = categoryRepository.findByIsActive(isActive, pageable);
            } else {
                categoryPage = categoryRepository.findAll(pageable);
            }
        }

        Map<Long, Long> productCountMap = getProductCountMap();

        List<CategoryResponse> responses = categoryPage.getContent().stream()
                .map(c -> categoryMapper.toResponse(c, productCountMap.getOrDefault(c.getId(), 0L)))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, categoryPage.getTotalElements());
    }

    private Map<Long, Long> getProductCountMap() {
        return productRepository.countProductsGroupedByCategoryId().stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private void logActivity(ActionType action, String entityType, Long entityId, String description, Long userId, String ipAddress) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                ActivityLog log = ActivityLog.builder()
                        .user(user)
                        .action(action)
                        .entityType(entityType)
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
