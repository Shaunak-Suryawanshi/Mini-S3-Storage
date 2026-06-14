package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUsageResponse {

    private Long userId;
    private String email;
    private long bucketCount;
    private long fileCount;
    private long totalSizeBytes;
}
