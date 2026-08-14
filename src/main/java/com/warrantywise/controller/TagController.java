package com.warrantywise.controller;

import com.warrantywise.dto.tag.TagRequest;
import com.warrantywise.dto.tag.TagResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.TagService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        TagResponse response = tagService.createTag(request, httpRequest.getRemoteAddr(), currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        TagResponse response = tagService.updateTag(id, request, httpRequest.getRemoteAddr(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        tagService.deleteTag(id, httpRequest.getRemoteAddr(), currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TagResponse response = tagService.getTagById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tags")
    public ResponseEntity<List<TagResponse>> getUserTags(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<TagResponse> responses = tagService.getUserTags(currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<TagResponse>> getUserTagsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<TagResponse> responses = tagService.getUserTagsPaginated(page, size, sortBy, sortDir, search, currentUser);
        return ResponseEntity.ok(responses);
    }
}
