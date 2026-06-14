package com.shaunak.mini_s3.service;

import com.shaunak.mini_s3.dto.DownloadLinkDetailsResponse;
import com.shaunak.mini_s3.dto.DownloadLinkResponse;
import com.shaunak.mini_s3.entity.Bucket;
import com.shaunak.mini_s3.entity.DownloadLink;
import com.shaunak.mini_s3.entity.FileMetadata;
import com.shaunak.mini_s3.entity.User;
import com.shaunak.mini_s3.exception.ResourceNotFoundException;
import com.shaunak.mini_s3.repository.BucketRepository;
import com.shaunak.mini_s3.repository.DownloadLinkRepository;
import com.shaunak.mini_s3.repository.FileMetadataRepository;
import com.shaunak.mini_s3.repository.UserRepository;
import com.shaunak.mini_s3.service.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SharingService {

    private final UserRepository userRepository;
    private final BucketRepository bucketRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final DownloadLinkRepository downloadLinkRepository;
    private final StorageService storageService;

    public SharingService(
            UserRepository userRepository,
            BucketRepository bucketRepository,
            FileMetadataRepository fileMetadataRepository,
            DownloadLinkRepository downloadLinkRepository,
            StorageService storageService
    ) {
        this.userRepository = userRepository;
        this.bucketRepository = bucketRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.downloadLinkRepository = downloadLinkRepository;
        this.storageService = storageService;
    }

    public DownloadLinkResponse createDownloadLink(
            Long bucketId,
            Long fileId,
            long expiresInMinutes,
            String ownerEmail
    ) {
        OwnedFile ownedFile = getOwnedFile(bucketId, fileId, ownerEmail);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(expiresInMinutes);

        DownloadLink downloadLink = DownloadLink.builder()
                .token(token)
                .expiryTime(expiryTime)
                .file(ownedFile.file())
                .createdBy(ownedFile.owner())
                .build();

        downloadLinkRepository.save(downloadLink);

        return new DownloadLinkResponse(
                token,
                "/api/public/downloads/" + token,
                expiryTime
        );
    }

    public List<DownloadLinkDetailsResponse> listDownloadLinks(
            Long bucketId,
            Long fileId,
            String ownerEmail
    ) {
        OwnedFile ownedFile = getOwnedFile(bucketId, fileId, ownerEmail);

        return downloadLinkRepository.findByFile(ownedFile.file())
                .stream()
                .map(this::mapToDetailsResponse)
                .toList();
    }

    public void revokeDownloadLink(Long bucketId, Long fileId, Long linkId, String ownerEmail) {
        OwnedFile ownedFile = getOwnedFile(bucketId, fileId, ownerEmail);

        DownloadLink downloadLink = downloadLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Download link not found"));

        if (!downloadLink.getFile().getId().equals(ownedFile.file().getId())) {
            throw new ResourceNotFoundException("Download link not found");
        }

        downloadLinkRepository.delete(downloadLink);
    }

    public SharedDownload getSharedDownload(String token) {
        DownloadLink downloadLink = downloadLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Download link not found"));

        if (downloadLink.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Download link expired");
        }

        FileMetadata file = downloadLink.getFile();
        Resource resource = storageService.load(file.getObjectKey());

        return new SharedDownload(file, resource);
    }

    public record SharedDownload(FileMetadata file, Resource resource) {
    }

    private OwnedFile getOwnedFile(Long bucketId, Long fileId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Bucket bucket = bucketRepository.findByIdAndOwner(bucketId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket not found"));

        FileMetadata file = fileMetadataRepository.findByIdAndBucket(fileId, bucket)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return new OwnedFile(owner, file);
    }

    private DownloadLinkDetailsResponse mapToDetailsResponse(DownloadLink downloadLink) {
        return new DownloadLinkDetailsResponse(
                downloadLink.getId(),
                downloadLink.getToken(),
                "/api/public/downloads/" + downloadLink.getToken(),
                downloadLink.getExpiryTime(),
                downloadLink.getCreatedAt(),
                downloadLink.getExpiryTime().isBefore(LocalDateTime.now())
        );
    }

    private record OwnedFile(User owner, FileMetadata file) {
    }
}
