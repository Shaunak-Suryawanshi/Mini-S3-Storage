package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BucketResponse {

    private Long id;
    private String bucketName;
    private Long ownerId;
    private String ownerEmail;
    private String visibility;
    private LocalDateTime createdAt;
}
