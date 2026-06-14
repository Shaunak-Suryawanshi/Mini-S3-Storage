package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FileResponse {

    private Long id;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String objectKey;
    private Long bucketId;
    private String bucketName;
    private Long uploadedBy;
    private LocalDateTime uploadDate;
}
