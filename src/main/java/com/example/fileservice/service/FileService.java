package com.example.fileservice.service;

import com.example.fileservice.controller.FileController;
import com.example.fileservice.exception.ResourceNotFoundException;
import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final Path storageLocation = Path.of("storage");


    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    public String uploadFile(String name, String contentType, Map<String, Object> meta, String source, Date expireTime, MultipartFile file) throws IOException {
        if (!Files.exists(storageLocation)) {
            Files.createDirectories(storageLocation);
        }

        String token = UUID.randomUUID().toString();
        Path targetLocation = storageLocation.resolve(token);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

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

        return token;
    }

    public FileMetadata getMetadata(UUID token) {
        List<FileMetadata> metadataList = fileMetadataRepository.findByTokenIn(Collections.singletonList(token));
        return metadataList.isEmpty() ? null : metadataList.get(0);
    }

    public File getFile(String token) {
        FileMetadata metadata = getMetadata(UUID.fromString(token));
        return metadata != null ? new File(metadata.getPath()) : null;
    }

    public Map<String, FileMetadata> getMetadataByTokens(List<UUID> tokens) {
        List<FileMetadata> metadataList = fileMetadataRepository.findByTokenIn(tokens);
        return metadataList.stream().collect(Collectors.toMap(
                metadata -> metadata.getToken().toString(),
                metadata -> metadata
        ));
    }

}
