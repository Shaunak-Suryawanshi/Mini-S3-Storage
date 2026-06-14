package com.shaunak.mini_s3.repository;

import com.shaunak.mini_s3.entity.DownloadLink;
import com.shaunak.mini_s3.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DownloadLinkRepository extends JpaRepository<DownloadLink, Long> {

    Optional<DownloadLink> findByToken(String token);

    List<DownloadLink> findByFile(FileMetadata file);
}
