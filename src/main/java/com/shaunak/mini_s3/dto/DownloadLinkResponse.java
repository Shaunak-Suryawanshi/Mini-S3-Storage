package com.shaunak.mini_s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DownloadLinkResponse {

    private String token;
    private String downloadUrl;
    private LocalDateTime expiryTime;
}
