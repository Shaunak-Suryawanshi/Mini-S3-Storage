package com.shaunak.mini_s3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBucketRequest {

    @NotBlank(message = "Bucket name is required")
    @Size(min = 3, max = 63, message = "Bucket name must be between 3 and 63 characters")
    @Pattern(
            regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$",
            message = "Bucket name must contain only lowercase letters, numbers, and hyphens"
    )
    private String bucketName;

    @NotBlank(message = "Visibility is required")
    @Pattern(regexp = "PUBLIC|PRIVATE", message = "Visibility must be PUBLIC or PRIVATE")
    private String visibility;
}
