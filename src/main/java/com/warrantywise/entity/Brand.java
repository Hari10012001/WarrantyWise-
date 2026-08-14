package com.warrantywise.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands", indexes = {@Index(name="idx_brands_name", columnList="name")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand extends BaseEntity {

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website", length = 500)
    private String website;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @OneToMany(mappedBy = "brand")
    private List<Product> products = new ArrayList<>();
}
