package com.shaunak.mini_s3.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileMetadata file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "downloaded_by", nullable = false)
    private User downloadedBy;

    private LocalDateTime downloadedAt;

    @PrePersist
    public void prePersist() {
        if (downloadedAt == null) {
            downloadedAt = LocalDateTime.now();
        }
    }
}
