package com.shaunak.mini_s3.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDownloadLinkRequest {

    @Min(value = 1, message = "Expiry must be at least 1 minute")
    @Max(value = 1440, message = "Expiry cannot exceed 1440 minutes")
    private long expiresInMinutes = 15;
}
