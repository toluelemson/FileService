package com.example.fileservice.service;

import com.example.fileservice.controller.exception.NotFoundException;
import com.example.fileservice.data.entities.Entity;
import com.example.fileservice.data.repository.EntityRepository;
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
    private final EntityRepository entityRepository;

    @Autowired
    public FileService(EntityRepository entityRepository) {

        this.storageLocation = Paths.get("storage").toAbsolutePath().normalize();
        this.entityRepository = entityRepository;


        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
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
            throw new IOException("Failed to store file", e);
        }

        long size = Files.size(targetLocation);
        Date createTime = new Date();

        Entity metadata = new Entity(
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

        entityRepository.save(metadata);
        return token;
    }

    @Transactional(readOnly = true)
    public Entity getMetadata(UUID token) {
        return entityRepository.findByTokenIn(Collections.singletonList(token))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("File metadata not found for token: " + token));
    }

    @Transactional(readOnly = true)
    public File getFile(String token) {
        Entity metadata = getMetadata(UUID.fromString(token));
        File file = new File(metadata.getPath());
        if (!file.exists()) {
            throw new NotFoundException("File does not exist for token: " + token);
        }
        if (!file.canRead()) {
            throw new NotFoundException("File cannot be read for token: " + token);
        }
        return file;
    }


    @Transactional(readOnly = true)
    public Map<String, Entity> getMetadataByTokens(List<UUID> tokens) {
        List<Entity> metadataList = entityRepository.findByTokenIn(tokens);
        return metadataList.stream().collect(Collectors.toMap(
                metadata -> metadata.getToken().toString(),
                metadata -> metadata
        ));
    }

    @Transactional
    public boolean deleteFileByToken(UUID token) {
        Entity metadata = getMetadata(token);
        entityRepository.deleteById(metadata.getToken());

        try {
            Files.deleteIfExists(Paths.get(metadata.getPath()));
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
