package com.shaunak.mini_s3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBucketVisibilityRequest {

    @NotBlank(message = "Visibility is required")
    @Pattern(regexp = "PUBLIC|PRIVATE", message = "Visibility must be PUBLIC or PRIVATE")
    private String visibility;
}
