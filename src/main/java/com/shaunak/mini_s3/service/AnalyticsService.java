package com.shaunak.mini_s3.service;

import com.shaunak.mini_s3.dto.BucketUsageResponse;
import com.shaunak.mini_s3.dto.DownloadActivityResponse;
import com.shaunak.mini_s3.dto.FileAnalyticsResponse;
import com.shaunak.mini_s3.dto.UserUsageResponse;
import com.shaunak.mini_s3.entity.Bucket;
import com.shaunak.mini_s3.entity.DownloadEvent;
import com.shaunak.mini_s3.entity.FileMetadata;
import com.shaunak.mini_s3.entity.User;
import com.shaunak.mini_s3.exception.ResourceNotFoundException;
import com.shaunak.mini_s3.repository.BucketRepository;
import com.shaunak.mini_s3.repository.DownloadEventRepository;
import com.shaunak.mini_s3.repository.FileMetadataRepository;
import com.shaunak.mini_s3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final BucketRepository bucketRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final DownloadEventRepository downloadEventRepository;

    public AnalyticsService(
            UserRepository userRepository,
            BucketRepository bucketRepository,
            FileMetadataRepository fileMetadataRepository,
            DownloadEventRepository downloadEventRepository
    ) {
        this.userRepository = userRepository;
        this.bucketRepository = bucketRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.downloadEventRepository = downloadEventRepository;
    }

    public FileAnalyticsResponse getFileAnalytics(Long bucketId, Long fileId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        FileMetadata file = fileMetadataRepository.findByIdAndBucket(fileId, bucket)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        long downloadCount = downloadEventRepository.countByFile(file);

        return new FileAnalyticsResponse(
                file.getId(),
                file.getFileName(),
                bucket.getId(),
                bucket.getBucketName(),
                downloadCount
        );
    }

    public BucketUsageResponse getBucketUsage(Long bucketId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        long fileCount = fileMetadataRepository.countByUploadedByAndBucket(owner, bucket);
        long totalSizeBytes = fileMetadataRepository.sumFileSizeByUploadedByAndBucket(owner, bucket);

        return new BucketUsageResponse(
                bucket.getId(),
                bucket.getBucketName(),
                fileCount,
                totalSizeBytes
        );
    }

    public UserUsageResponse getUserUsage(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        long bucketCount = bucketRepository.findByOwner(owner).size();
        long fileCount = fileMetadataRepository.countByUploadedBy(owner);
        long totalSizeBytes = fileMetadataRepository.sumFileSizeByUploadedBy(owner);

        return new UserUsageResponse(
                owner.getId(),
                owner.getEmail(),
                bucketCount,
                fileCount,
                totalSizeBytes
        );
    }

    public List<DownloadActivityResponse> getUserDownloadActivity(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        return downloadEventRepository.findByDownloadedByOrderByDownloadedAtDesc(owner)
                .stream()
                .map(this::mapToDownloadActivityResponse)
                .toList();
    }

    private DownloadActivityResponse mapToDownloadActivityResponse(DownloadEvent downloadEvent) {
        FileMetadata file = downloadEvent.getFile();
        Bucket bucket = file.getBucket();

        return new DownloadActivityResponse(
                downloadEvent.getId(),
                file.getId(),
                file.getFileName(),
                bucket.getId(),
                bucket.getBucketName(),
                downloadEvent.getDownloadedAt()
        );
    }
}
