package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DownloadLinkDetailsResponse {

    private Long id;
    private String token;
    private String downloadUrl;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt;
    private boolean expired;
}
