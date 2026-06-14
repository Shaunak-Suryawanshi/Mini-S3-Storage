package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileAnalyticsResponse {

    private Long fileId;
    private String fileName;
    private Long bucketId;
    private String bucketName;
    private long downloadCount;
}
