package com.warrantywise.config;

import com.warrantywise.entity.Brand;
import com.warrantywise.entity.Category;
import com.warrantywise.repository.BrandRepository;
import com.warrantywise.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    public void run(String... args) {
        initCategories();
        initBrands();
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Initializing default product categories...");
            List<Category> categories = List.of(
                    Category.builder().name("Mobile").description("Smartphones, Mobile Phones & Accessories").icon("bi-phone").isActive(true).build(),
                    Category.builder().name("Laptop & Computers").description("Laptops, Desktop PCs & Workstations").icon("bi-laptop").isActive(true).build(),
                    Category.builder().name("Home Appliances").description("Washing Machines, Refrigerators, ACs & Microwaves").icon("bi-house-gear").isActive(true).build(),
                    Category.builder().name("Audio & Wearables").description("Headphones, Earbuds, Smartwatches & Speakers").icon("bi-headphones").isActive(true).build(),
                    Category.builder().name("Television & Display").description("Smart TVs, Monitors & Projectors").icon("bi-tv").isActive(true).build(),
                    Category.builder().name("Cameras & Photography").description("DSLR, Mirrorless Cameras & Action Cams").icon("bi-camera").isActive(true).build(),
                    Category.builder().name("Smart Home & IoT").description("Smart Hubs, Security Cameras & Sensors").icon("bi-wifi").isActive(true).build(),
                    Category.builder().name("General").description("General Products & Appliances").icon("bi-box-seam").isActive(true).build()
            );
            categoryRepository.saveAll(categories);
            log.info("Successfully seeded {} default categories.", categories.size());
        }
    }

    private void initBrands() {
        if (brandRepository.count() == 0) {
            log.info("Initializing default product brands...");
            List<Brand> brands = List.of(
                    Brand.builder().name("Samsung").website("https://www.samsung.com").isActive(true).build(),
                    Brand.builder().name("Apple").website("https://www.apple.com").isActive(true).build(),
                    Brand.builder().name("Sony").website("https://www.sony.com").isActive(true).build(),
                    Brand.builder().name("LG").website("https://www.lg.com").isActive(true).build(),
                    Brand.builder().name("Dell").website("https://www.dell.com").isActive(true).build(),
                    Brand.builder().name("HP").website("https://www.hp.com").isActive(true).build(),
                    Brand.builder().name("Lenovo").website("https://www.lenovo.com").isActive(true).build(),
                    Brand.builder().name("Asus").website("https://www.asus.com").isActive(true).build(),
                    Brand.builder().name("OnePlus").website("https://www.oneplus.com").isActive(true).build(),
                    Brand.builder().name("Xiaomi").website("https://www.mi.com").isActive(true).build(),
                    Brand.builder().name("Bosch").website("https://www.bosch.com").isActive(true).build(),
                    Brand.builder().name("Whirlpool").website("https://www.whirlpool.com").isActive(true).build(),
                    Brand.builder().name("Bose").website("https://www.bose.com").isActive(true).build(),
                    Brand.builder().name("Generic").website("").isActive(true).build()
            );
            brandRepository.saveAll(brands);
            log.info("Successfully seeded {} default brands.", brands.size());
        }
    }
}
