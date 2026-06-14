package com.shaunak.mini_s3.repository;

import com.shaunak.mini_s3.entity.Bucket;
import com.shaunak.mini_s3.entity.FileMetadata;
import com.shaunak.mini_s3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByBucket(Bucket bucket);

    Optional<FileMetadata> findByIdAndBucket(Long id, Bucket bucket);

    List<FileMetadata> findByBucketAndFileNameContainingIgnoreCase(Bucket bucket, String fileName);

    long countByBucket(Bucket bucket);

    long countByUploadedBy(User uploadedBy);

    long countByUploadedByAndBucket(User uploadedBy, Bucket bucket);

    @Query("select coalesce(sum(file.fileSize), 0) from FileMetadata file where file.bucket = :bucket")
    Long sumFileSizeByBucket(Bucket bucket);

    @Query("select coalesce(sum(file.fileSize), 0) from FileMetadata file where file.uploadedBy = :uploadedBy")
    Long sumFileSizeByUploadedBy(User uploadedBy);

    @Query("select coalesce(sum(file.fileSize), 0) from FileMetadata file where file.uploadedBy = :uploadedBy and file.bucket = :bucket")
    Long sumFileSizeByUploadedByAndBucket(User uploadedBy, Bucket bucket);
}
