package com.warrantywise.service.impl;

import com.warrantywise.dto.tag.TagRequest;
import com.warrantywise.dto.tag.TagResponse;
import com.warrantywise.entity.ActivityLog;
import com.warrantywise.entity.Tag;
import com.warrantywise.entity.User;
import com.warrantywise.enums.ActionType;
import com.warrantywise.exception.DuplicateResourceException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.exception.UnauthorizedException;
import com.warrantywise.mapper.TagMapper;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.TagRepository;
import com.warrantywise.repository.UserRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final ProductRepository productRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;

    @Override
    public TagResponse createTag(TagRequest request, String ipAddress, UserPrincipal currentUser) {
        if (tagRepository.existsByNameIgnoreCaseAndUserId(request.getName(), currentUser.getId())) {
            throw new DuplicateResourceException("Tag '" + request.getName() + "' already exists");
        }
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Tag tag = tagMapper.toEntity(request, user);
        Tag savedTag = tagRepository.save(tag);

        logActivity(ActionType.CREATE, "TAG", savedTag.getId(), "Created tag: " + savedTag.getName(), user, ipAddress);

        return tagMapper.toResponse(savedTag, 0L);
    }

    @Override
    public TagResponse updateTag(Long id, TagRequest request, String ipAddress, UserPrincipal currentUser) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));

        if (!tag.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to modify this tag");
        }

        if (!tag.getName().equalsIgnoreCase(request.getName()) &&
                tagRepository.existsByNameIgnoreCaseAndUserId(request.getName(), currentUser.getId())) {
            throw new DuplicateResourceException("Tag '" + request.getName() + "' already exists");
        }

        tagMapper.updateEntityFromRequest(request, tag);
        Tag updated = tagRepository.save(tag);

        Map<Long, Long> productCountMap = getProductCountMap(currentUser.getId());
        long count = productCountMap.getOrDefault(id, 0L);

        logActivity(ActionType.UPDATE, "TAG", id, "Updated tag: " + updated.getName(), tag.getUser(), ipAddress);

        return tagMapper.toResponse(updated, count);
    }

    @Override
    public void deleteTag(Long id, String ipAddress, UserPrincipal currentUser) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));

        if (!tag.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to delete this tag");
        }

        tagRepository.delete(tag);

        logActivity(ActionType.DELETE, "TAG", id, "Deleted tag: " + tag.getName(), tag.getUser(), ipAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long id, UserPrincipal currentUser) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));

        if (!tag.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        Map<Long, Long> productCountMap = getProductCountMap(currentUser.getId());
        return tagMapper.toResponse(tag, productCountMap.getOrDefault(id, 0L));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getUserTags(UserPrincipal currentUser) {
        List<Tag> list = tagRepository.findByUserIdOrderByNameAsc(currentUser.getId());
        Map<Long, Long> productCountMap = getProductCountMap(currentUser.getId());

        return list.stream()
                .map(t -> tagMapper.toResponse(t, productCountMap.getOrDefault(t.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> getUserTagsPaginated(int page, int size, String sortBy, String sortDir, String search, UserPrincipal currentUser) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Tag> tagPage;
        if (search != null && !search.trim().isEmpty()) {
            tagPage = tagRepository.findByUserIdAndNameContainingIgnoreCase(currentUser.getId(), search, pageable);
        } else {
            tagPage = tagRepository.findByUserId(currentUser.getId(), pageable);
        }

        Map<Long, Long> productCountMap = getProductCountMap(currentUser.getId());

        List<TagResponse> responses = tagPage.getContent().stream()
                .map(t -> tagMapper.toResponse(t, productCountMap.getOrDefault(t.getId(), 0L)))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, tagPage.getTotalElements());
    }

    private Map<Long, Long> getProductCountMap(Long userId) {
        return productRepository.countProductsGroupedByTagIdForUser(userId).stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private void logActivity(ActionType action, String entityType, Long entityId, String description, User user, String ipAddress) {
        try {
            ActivityLog log = ActivityLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(ipAddress)
                    .build();
            activityLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
}
