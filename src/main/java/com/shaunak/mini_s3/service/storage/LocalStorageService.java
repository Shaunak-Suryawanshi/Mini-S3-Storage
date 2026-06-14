package com.shaunak.mini_s3.service.storage;

import com.shaunak.mini_s3.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private static final Path STORAGE_ROOT = Path.of("storage");

    @Override
    public String store(String bucketName, MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "uploaded-file" : file.getOriginalFilename()
        );
        String objectKey = bucketName + "/" + UUID.randomUUID() + "-" + originalFileName;

        try {
            Path targetPath = STORAGE_ROOT.resolve(objectKey).normalize();
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
            return objectKey;
        } catch (IOException exception) {
            throw new RuntimeException("Failed to store file");
        }
    }

    @Override
    public Resource load(String objectKey) {
        try {
            Path filePath = STORAGE_ROOT.resolve(objectKey).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Stored file not found");
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Failed to load file");
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Path filePath = STORAGE_ROOT.resolve(objectKey).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to delete file");
        }
    }
}
