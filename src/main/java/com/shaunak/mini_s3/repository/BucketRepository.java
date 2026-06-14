package com.shaunak.mini_s3.repository;

import com.shaunak.mini_s3.entity.Bucket;
import com.shaunak.mini_s3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BucketRepository extends JpaRepository<Bucket, Long> {

    boolean existsByBucketName(String bucketName);

    Optional<Bucket> findByBucketName(String bucketName);

    List<Bucket> findByOwner(User owner);

    Optional<Bucket> findByIdAndOwner(Long id, User owner);
}
