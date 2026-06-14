package com.shaunak.mini_s3.controller;

import com.shaunak.mini_s3.dto.BucketUsageResponse;
import com.shaunak.mini_s3.dto.DownloadActivityResponse;
import com.shaunak.mini_s3.dto.FileAnalyticsResponse;
import com.shaunak.mini_s3.dto.UserUsageResponse;
import com.shaunak.mini_s3.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/buckets/{bucketId}/files/{fileId}/analytics")
    public ResponseEntity<FileAnalyticsResponse> getFileAnalytics(
            @PathVariable Long bucketId,
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        FileAnalyticsResponse response = analyticsService.getFileAnalytics(
                bucketId,
                fileId,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/buckets/{bucketId}/analytics/usage")
    public ResponseEntity<BucketUsageResponse> getBucketUsage(
            @PathVariable Long bucketId,
            Authentication authentication
    ) {
        BucketUsageResponse response = analyticsService.getBucketUsage(
                bucketId,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/me/usage")
    public ResponseEntity<UserUsageResponse> getUserUsage(Authentication authentication) {
        UserUsageResponse response = analyticsService.getUserUsage(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/me/downloads")
    public ResponseEntity<List<DownloadActivityResponse>> getUserDownloadActivity(
            Authentication authentication
    ) {
        List<DownloadActivityResponse> response = analyticsService.getUserDownloadActivity(
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }
}
