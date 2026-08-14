package com.warrantywise.mapper;

import com.warrantywise.dto.tag.TagRequest;
import com.warrantywise.dto.tag.TagResponse;
import com.warrantywise.entity.Tag;
import com.warrantywise.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public Tag toEntity(TagRequest request, User user) {
        if (request == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setUser(user);
        return tag;
    }

    public TagResponse toResponse(Tag tag, Long productCount) {
        if (tag == null) {
            return null;
        }

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .productCount(productCount != null ? productCount : 0L)
                .build();
    }

    public void updateEntityFromRequest(TagRequest request, Tag tag) {
        if (request == null || tag == null) {
            return;
        }

        if (request.getName() != null) {
            tag.setName(request.getName());
        }
    }
}
