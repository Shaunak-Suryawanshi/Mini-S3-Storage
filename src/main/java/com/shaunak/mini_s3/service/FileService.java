package com.shaunak.mini_s3.service;

import com.shaunak.mini_s3.dto.FileDownloadResponse;
import com.shaunak.mini_s3.dto.FileResponse;
import com.shaunak.mini_s3.entity.Bucket;
import com.shaunak.mini_s3.entity.DownloadEvent;
import com.shaunak.mini_s3.entity.FileMetadata;
import com.shaunak.mini_s3.entity.User;
import com.shaunak.mini_s3.exception.ResourceNotFoundException;
import com.shaunak.mini_s3.repository.BucketRepository;
import com.shaunak.mini_s3.repository.DownloadEventRepository;
import com.shaunak.mini_s3.repository.FileMetadataRepository;
import com.shaunak.mini_s3.repository.UserRepository;
import com.shaunak.mini_s3.service.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final BucketRepository bucketRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final DownloadEventRepository downloadEventRepository;

    public FileService(
            FileMetadataRepository fileMetadataRepository,
            BucketRepository bucketRepository,
            UserRepository userRepository,
            StorageService storageService,
            DownloadEventRepository downloadEventRepository
    ) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.bucketRepository = bucketRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.downloadEventRepository = downloadEventRepository;
    }

    public FileResponse uploadFile(Long bucketId, MultipartFile file, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        String objectKey = storageService.store(bucket.getBucketName(), file);

        FileMetadata metadata = FileMetadata.builder()
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .objectKey(objectKey)
                .bucket(bucket)
                .uploadedBy(owner)
                .build();

        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);
        return mapToResponse(savedMetadata);
    }

    public List<FileResponse> listFiles(Long bucketId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        return fileMetadataRepository.findByBucket(bucket)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FileResponse getFile(Long bucketId, Long fileId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        FileMetadata metadata = fileMetadataRepository.findByIdAndBucket(fileId, bucket)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return mapToResponse(metadata);
    }

    public List<FileResponse> searchFiles(Long bucketId, String query, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        return fileMetadataRepository.findByBucketAndFileNameContainingIgnoreCase(bucket, query)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<FileResponse> listPublicFiles(String bucketName) {
        Bucket bucket = getPublicBucket(bucketName);

        return fileMetadataRepository.findByBucket(bucket)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FileResponse getPublicFile(String bucketName, Long fileId) {
        Bucket bucket = getPublicBucket(bucketName);

        FileMetadata metadata = fileMetadataRepository.findByIdAndBucket(fileId, bucket)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return mapToResponse(metadata);
    }

    public FileDownloadResponse downloadPublicFile(String bucketName, Long fileId) {
        Bucket bucket = getPublicBucket(bucketName);

        FileMetadata metadata = fileMetadataRepository.findByIdAndBucket(fileId, bucket)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return new FileDownloadResponse(metadata, storageService.load(metadata.getObjectKey()));
    }

    public FileDownloadResponse downloadFile(Long bucketId, Long fileId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        FileMetadata metadata = fileMetadataRepository.findByIdAndBucket(fileId, bucket)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        Resource resource = storageService.load(metadata.getObjectKey());

        DownloadEvent downloadEvent = DownloadEvent.builder()
                .file(metadata)
                .downloadedBy(owner)
                .build();
        downloadEventRepository.save(downloadEvent);

        return new FileDownloadResponse(metadata, resource);
    }

    public void deleteFile(Long bucketId, Long fileId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        FileMetadata metadata = fileMetadataRepository.findByIdAndBucket(fileId, bucket)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        storageService.delete(metadata.getObjectKey());
        fileMetadataRepository.delete(metadata);
    }

    private FileResponse mapToResponse(FileMetadata metadata) {
        return new FileResponse(
                metadata.getId(),
                metadata.getFileName(),
                metadata.getFileSize(),
                metadata.getContentType(),
                metadata.getObjectKey(),
                metadata.getBucket().getId(),
                metadata.getBucket().getBucketName(),
                metadata.getUploadedBy().getId(),
                metadata.getUploadDate()
        );
    }

    private Bucket getPublicBucket(String bucketName) {
        Bucket bucket = bucketRepository.findByBucketName(bucketName)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        if (!"PUBLIC".equals(bucket.getVisibility())) {
            throw new ResourceNotFoundException("Bucket not found");
        }

        return bucket;
    }
}
