package com.shaunak.mini_s3.controller;

import com.shaunak.mini_s3.dto.BucketResponse;
import com.shaunak.mini_s3.dto.CreateBucketRequest;
import com.shaunak.mini_s3.dto.UpdateBucketVisibilityRequest;
import com.shaunak.mini_s3.service.BucketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buckets")
public class BucketController {

    private final BucketService bucketService;

    public BucketController(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @PostMapping
    public ResponseEntity<BucketResponse> createBucket(
            @Valid @RequestBody CreateBucketRequest request,
            Authentication authentication
    ) {
        BucketResponse response = bucketService.createBucket(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BucketResponse>> listBuckets(Authentication authentication) {
        List<BucketResponse> response = bucketService.listBuckets(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bucketId}")
    public ResponseEntity<BucketResponse> getBucket(
            @PathVariable Long bucketId,
            Authentication authentication
    ) {
        BucketResponse response = bucketService.getBucket(bucketId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bucketId}")
    public ResponseEntity<Void> deleteBucket(
            @PathVariable Long bucketId,
            Authentication authentication
    ) {
        bucketService.deleteBucket(bucketId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{bucketId}/visibility")
    public ResponseEntity<BucketResponse> updateVisibility(
            @PathVariable Long bucketId,
            @Valid @RequestBody UpdateBucketVisibilityRequest request,
            Authentication authentication
    ) {
        BucketResponse response = bucketService.updateVisibility(
                bucketId,
                request.getVisibility(),
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }
}
