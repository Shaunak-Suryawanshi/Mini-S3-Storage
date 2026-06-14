package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BucketUsageResponse {

    private Long bucketId;
    private String bucketName;
    private long fileCount;
    private long totalSizeBytes;
}
