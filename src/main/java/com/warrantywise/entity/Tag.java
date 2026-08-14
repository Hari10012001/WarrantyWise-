package com.warrantywise.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags", uniqueConstraints = {@UniqueConstraint(name="uk_tag_name_user", columnNames={"name", "user_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> products = new HashSet<>();

    @Column(name = "name", length = 50, nullable = false)
    private String name;
}
