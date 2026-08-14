package com.warrantywise.dto.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRequest {

    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Logo URL cannot exceed 500 characters")
    private String logoUrl;

    @Size(max = 500, message = "Website cannot exceed 500 characters")
    private String website;

    @Builder.Default
    private Boolean isActive = true;
}
