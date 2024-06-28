package com.example.fileservice.service;

import com.example.fileservice.exception.ResourceNotFoundException;
import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.repository.FileMetadataRepository;
import com.example.fileservice.util.CustomLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final Path storageLocation;
    private final FileMetadataRepository fileMetadataRepository;
    private final CustomLogger customLogger;

    @Autowired
    public FileService(FileMetadataRepository fileMetadataRepository, CustomLogger customLogger) {
        this.storageLocation = Paths.get("storage").toAbsolutePath().normalize();
        this.fileMetadataRepository = fileMetadataRepository;
        this.customLogger = customLogger;

        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            customLogger.logCritical(e);
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    @Transactional
    public String uploadFile(String name, String contentType, Map<String, Object> meta, String source, Date expireTime, MultipartFile file) throws IOException {
        String token = UUID.randomUUID().toString();
        Path targetLocation = storageLocation.resolve(token);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            customLogger.error("Failed to store file", e);
            throw new IOException("Failed to store file", e);
        }

        long size = Files.size(targetLocation);
        Date createTime = new Date();

        FileMetadata metadata = new FileMetadata(
                UUID.randomUUID(),
                name,
                contentType != null ? contentType : "application/octet-stream",
                size,
                createTime,
                meta,
                source,
                expireTime,
                targetLocation.toString(),
                UUID.fromString(token)
        );

        fileMetadataRepository.save(metadata);

        customLogger.info("File uploaded successfully: " + name);
        return token;
    }

    @Transactional(readOnly = true)
    public FileMetadata getMetadata(UUID token) {
        return fileMetadataRepository.findByTokenIn(Collections.singletonList(token))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("File metadata not found for token: " + token));
    }

    @Transactional(readOnly = true)
    public File getFile(String token) {
        FileMetadata metadata = getMetadata(UUID.fromString(token));
        File file = new File(metadata.getPath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("File does not exist for token: " + token);
        }
        if (!file.canRead()) {
            throw new ResourceNotFoundException("File cannot be read for token: " + token);
        }
        return file;
    }


    @Transactional(readOnly = true)
    public Map<String, FileMetadata> getMetadataByTokens(List<UUID> tokens) {
        List<FileMetadata> metadataList = fileMetadataRepository.findByTokenIn(tokens);
        return metadataList.stream().collect(Collectors.toMap(
                metadata -> metadata.getToken().toString(),
                metadata -> metadata
        ));
    }

    @Transactional
    public boolean deleteFileByToken(UUID token) {
        FileMetadata metadata = getMetadata(token);
        fileMetadataRepository.deleteById(metadata.getToken());

        try {
            Files.deleteIfExists(Paths.get(metadata.getPath()));
            customLogger.info("File deleted successfully for token: " + token);
            return true;
        } catch (IOException e) {
            customLogger.error("Failed to delete file for token: " + token, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
