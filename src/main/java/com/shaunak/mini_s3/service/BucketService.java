package com.shaunak.mini_s3.service;

import com.shaunak.mini_s3.dto.BucketResponse;
import com.shaunak.mini_s3.dto.CreateBucketRequest;
import com.shaunak.mini_s3.entity.Bucket;
import com.shaunak.mini_s3.entity.User;
import com.shaunak.mini_s3.exception.DuplicateResourceException;
import com.shaunak.mini_s3.exception.ResourceNotFoundException;
import com.shaunak.mini_s3.repository.BucketRepository;
import com.shaunak.mini_s3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BucketService {

    private final BucketRepository bucketRepository;
    private final UserRepository userRepository;

    public BucketService(BucketRepository bucketRepository, UserRepository userRepository) {
        this.bucketRepository = bucketRepository;
        this.userRepository = userRepository;
    }

    public BucketResponse createBucket(CreateBucketRequest request, String ownerEmail) {
        if (bucketRepository.existsByBucketName(request.getBucketName())) {
            throw new DuplicateResourceException("Bucket name already exists");
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = Bucket.builder()
                .bucketName(request.getBucketName())
                .owner(owner)
                .visibility(request.getVisibility())
                .build();

        Bucket savedBucket = bucketRepository.save(bucket);
        return mapToResponse(savedBucket);
    }

    public List<BucketResponse> listBuckets(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        return bucketRepository.findByOwner(owner)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BucketResponse getBucket(Long bucketId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        return mapToResponse(bucket);
    }

    public void deleteBucket(Long bucketId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        bucketRepository.delete(bucket);
    }

    public BucketResponse updateVisibility(Long bucketId, String visibility, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        bucket.setVisibility(visibility);
        Bucket savedBucket = bucketRepository.save(bucket);

        return mapToResponse(savedBucket);
    }

    private BucketResponse mapToResponse(Bucket bucket) {
        return new BucketResponse(
                bucket.getId(),
                bucket.getBucketName(),
                bucket.getOwner().getId(),
                bucket.getOwner().getEmail(),
                bucket.getVisibility(),
                bucket.getCreatedAt()
        );
    }
}
