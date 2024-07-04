package com.example.fileservice.controller;

import com.example.fileservice.dto.FileUploadRequest;
import com.example.fileservice.controller.exception.InternalException;
import com.example.fileservice.controller.exception.NotFoundException;
import com.example.fileservice.data.entities.Entity;
import com.example.fileservice.service.FileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/file")
@Validated
public class FileController {

    private final FileService fileService;
    private final ObjectMapper objectMapper;

    public FileController(FileService fileService, ObjectMapper objectMapper) {
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@Valid @ModelAttribute FileUploadRequest request) {
        try {
            Map<String, Object> metaMap = objectMapper.readValue(request.getMeta(), new TypeReference<>() {
            });
            String token = fileService.uploadFile(
                    request.getName(),
                    request.getContentType(),
                    metaMap,
                    request.getSource(),
                    request.getExpireTime(),
                    request.getContent()
            );

            Map<String, String> response = Collections.singletonMap("token", token);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid metadata: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new InternalException("Internal server error: " + e.getMessage(), e);
        }
    }

    @PostMapping("/metas")
    public ResponseEntity<Map<String, Object>> getFileMetadata(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> tokens = request.get("tokens");
            if (tokens == null || tokens.isEmpty()) {
                throw new IllegalArgumentException("Tokens list cannot be null or empty");
            }

            List<UUID> uuids = tokens.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            Map<String, Entity> metadataMap = fileService.getMetadataByTokens(uuids);

            Map<String, Object> response = metadataMap.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> Map.of(
                                    "token", entry.getKey(),
                                    "filename", entry.getValue().getName(),
                                    "size", entry.getValue().getSize(),
                                    "contentType", entry.getValue().getContentType(),
                                    "createTime", entry.getValue().getCreateTime().toInstant().toString(),
                                    "meta", entry.getValue().getMeta()
                            )
                    ));

            return ResponseEntity.ok(Map.of("files", response));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new InternalException("Internal server error: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{token}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String token) {
        try {
            File file = fileService.getFile(token);
            if (file == null) {
                throw new NotFoundException("File not found for token: " + token);
            }

            Path path = file.toPath();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("File not readable for token: " + token);
            }

            Entity metadata = fileService.getMetadata(UUID.fromString(token));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .header("X-Filename", file.getName())
                    .header("X-Filesize", String.valueOf(file.length()))
                    .header("X-CreateTime", metadata.getCreateTime().toInstant().toString())
                    .body(resource);
        } catch (Exception e) {
            throw new InternalException("Internal server error: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID token) {
        try {
            boolean isDeleted = fileService.deleteFileByToken(token);
            if (!isDeleted) {
                throw new NotFoundException("File not found for token: " + token);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new InternalException("Internal server error: " + e.getMessage(), e);
        }
    }
}
