package com.shaunak.mini_s3.dto;

import com.shaunak.mini_s3.entity.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class FileDownloadResponse {

    private FileMetadata metadata;
    private Resource resource;
}
