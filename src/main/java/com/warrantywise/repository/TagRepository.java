package com.warrantywise.repository;

import com.warrantywise.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserIdOrderByNameAsc(Long userId);

    Optional<Tag> findByNameAndUserId(String name, Long userId);

    Boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    Page<Tag> findByUserIdAndNameContainingIgnoreCase(Long userId, String name, Pageable pageable);

    Page<Tag> findByUserId(Long userId, Pageable pageable);
}

