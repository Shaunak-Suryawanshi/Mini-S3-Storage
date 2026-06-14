package com.shaunak.mini_s3.controller;

import com.shaunak.mini_s3.dto.FileDownloadResponse;
import com.shaunak.mini_s3.dto.FileResponse;
import com.shaunak.mini_s3.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/buckets/{bucketName}/files")
public class PublicBucketController {

    private final FileService fileService;

    public PublicBucketController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> listPublicFiles(@PathVariable String bucketName) {
        List<FileResponse> response = fileService.listPublicFiles(bucketName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getPublicFile(
            @PathVariable String bucketName,
            @PathVariable Long fileId
    ) {
        FileResponse response = fileService.getPublicFile(bucketName, fileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadPublicFile(
            @PathVariable String bucketName,
            @PathVariable Long fileId
    ) {
        FileDownloadResponse response = fileService.downloadPublicFile(bucketName, fileId);

        String contentType = response.getMetadata().getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : response.getMetadata().getContentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + response.getMetadata().getFileName() + "\""
                )
                .body(response.getResource());
    }
}
