package com.shaunak.mini_s3.service.storage;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;

public interface StorageService {

    String store(String bucketName, MultipartFile file);

    Resource load(String objectKey);

    void delete(String objectKey);
}
