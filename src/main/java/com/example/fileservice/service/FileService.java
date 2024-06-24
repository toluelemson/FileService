package com.example.fileservice.service;

import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.StandardCopyOption;
import java.util.*;


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

}
