package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DownloadActivityResponse {

    private Long downloadEventId;
    private Long fileId;
    private String fileName;
    private Long bucketId;
    private String bucketName;
    private LocalDateTime downloadedAt;
}
