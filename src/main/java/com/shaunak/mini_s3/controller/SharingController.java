package com.shaunak.mini_s3.controller;

import com.shaunak.mini_s3.dto.CreateDownloadLinkRequest;
import com.shaunak.mini_s3.dto.DownloadLinkDetailsResponse;
import com.shaunak.mini_s3.dto.DownloadLinkResponse;
import com.shaunak.mini_s3.service.SharingService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SharingController {

    private final SharingService sharingService;

    public SharingController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @PostMapping("/api/buckets/{bucketId}/files/{fileId}/links")
    public ResponseEntity<DownloadLinkResponse> createDownloadLink(
            @PathVariable Long bucketId,
            @PathVariable Long fileId,
            @Valid @RequestBody CreateDownloadLinkRequest request,
            Authentication authentication
    ) {
        DownloadLinkResponse response = sharingService.createDownloadLink(
                bucketId,
                fileId,
                request.getExpiresInMinutes(),
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/buckets/{bucketId}/files/{fileId}/links")
    public ResponseEntity<List<DownloadLinkDetailsResponse>> listDownloadLinks(
            @PathVariable Long bucketId,
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        List<DownloadLinkDetailsResponse> response = sharingService.listDownloadLinks(
                bucketId,
                fileId,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/buckets/{bucketId}/files/{fileId}/links/{linkId}")
    public ResponseEntity<Void> revokeDownloadLink(
            @PathVariable Long bucketId,
            @PathVariable Long fileId,
            @PathVariable Long linkId,
            Authentication authentication
    ) {
        sharingService.revokeDownloadLink(
                bucketId,
                fileId,
                linkId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/public/downloads/{token}")
    public ResponseEntity<Resource> downloadByToken(@PathVariable String token) {
        SharingService.SharedDownload response = sharingService.getSharedDownload(token);

        String contentType = response.file().getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : response.file().getContentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + response.file().getFileName() + "\""
                )
                .body(response.resource());
    }
}
