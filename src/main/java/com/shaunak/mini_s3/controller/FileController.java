package com.shaunak.mini_s3.controller;

import com.shaunak.mini_s3.dto.FileDownloadResponse;
import com.shaunak.mini_s3.dto.FileResponse;
import com.shaunak.mini_s3.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/buckets/{bucketId}/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<FileResponse> uploadFile(
            @PathVariable Long bucketId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        FileResponse response = fileService.uploadFile(bucketId, file, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> listFiles(
            @PathVariable Long bucketId,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        List<FileResponse> response = search == null || search.isBlank()
                ? fileService.listFiles(bucketId, authentication.getName())
                : fileService.searchFiles(bucketId, search, authentication.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getFile(
            @PathVariable Long bucketId,
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        FileResponse response = fileService.getFile(bucketId, fileId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long bucketId,
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        FileDownloadResponse response = fileService.downloadFile(
                bucketId,
                fileId,
                authentication.getName()
        );

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

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long bucketId,
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        fileService.deleteFile(bucketId, fileId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
