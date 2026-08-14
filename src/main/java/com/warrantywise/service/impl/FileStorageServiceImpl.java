package com.warrantywise.service.impl;

import com.warrantywise.exception.BadRequestException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadPath;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".pdf", ".doc", ".docx");

    public FileStorageServiceImpl(@Value("${app.file.upload-dir:./uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        if (originalFileName.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
        }

        String fileExtension = "";
        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            fileExtension = originalFileName.substring(lastDotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BadRequestException("File extension '" + fileExtension + "' is not allowed. Only images (jpg, png) and documents (pdf, doc) are supported.");
        }

        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + originalFileName, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(this.uploadPath)) {
                throw new BadRequestException("Path traversal attempt detected");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "fileName", fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File", "fileName", fileName);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.uploadPath.resolve(fileName).normalize();
            if (filePath.startsWith(this.uploadPath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            System.err.println("Could not delete file " + fileName + ": " + ex.getMessage());
        }
    }
}
