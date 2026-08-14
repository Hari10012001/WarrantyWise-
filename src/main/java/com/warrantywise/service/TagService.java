package com.warrantywise.service;

import com.warrantywise.dto.tag.TagRequest;
import com.warrantywise.dto.tag.TagResponse;
import com.warrantywise.security.UserPrincipal;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TagService {
    TagResponse createTag(TagRequest request, String ipAddress, UserPrincipal currentUser);
    TagResponse updateTag(Long id, TagRequest request, String ipAddress, UserPrincipal currentUser);
    void deleteTag(Long id, String ipAddress, UserPrincipal currentUser);
    TagResponse getTagById(Long id, UserPrincipal currentUser);
    List<TagResponse> getUserTags(UserPrincipal currentUser);
    Page<TagResponse> getUserTagsPaginated(int page, int size, String sortBy, String sortDir, String search, UserPrincipal currentUser);
}
