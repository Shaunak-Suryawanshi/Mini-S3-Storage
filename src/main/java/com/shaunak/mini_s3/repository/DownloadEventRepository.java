package com.shaunak.mini_s3.repository;

import com.shaunak.mini_s3.entity.DownloadEvent;
import com.shaunak.mini_s3.entity.FileMetadata;
import com.shaunak.mini_s3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DownloadEventRepository extends JpaRepository<DownloadEvent, Long> {

    long countByFile(FileMetadata file);

    List<DownloadEvent> findByDownloadedByOrderByDownloadedAtDesc(User downloadedBy);
}
