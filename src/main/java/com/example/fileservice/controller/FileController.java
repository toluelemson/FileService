package com.example.fileservice.controller;

import com.example.fileservice.dto.FileUploadRequest;
import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.service.FileService;
import com.example.fileservice.util.CustomLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/file")
@Validated
public class FileController {

    private final FileService fileService;
    private final ObjectMapper objectMapper;
    private final CustomLogger customLogger;

    public FileController(FileService fileService, ObjectMapper objectMapper, CustomLogger customLogger) {
        this.fileService = fileService;
        this.objectMapper = objectMapper;
        this.customLogger = customLogger;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@Valid @ModelAttribute FileUploadRequest request) {
        try {
            Map<String, Object> metaMap = objectMapper.readValue(request.getMeta(), Map.class);
            String token = fileService.uploadFile(request.getName(), request.getContentType(), metaMap, request.getSource(), request.getExpireTime(), request.getContent());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            customLogger.info("File uploaded successfully: " + request.getName());
            return ResponseEntity.status(201).body(response);
        } catch (IOException e) {
            customLogger.error("Failed to upload file: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid meta data: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            customLogger.error("Invalid argument: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            customLogger.logCritical(e);
            return ResponseEntity.status(503).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/metas")
    public ResponseEntity<Map<String, Object>> getFileMetadata(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> tokens = request.get("tokens");
            if (tokens == null || tokens.isEmpty()) {
                throw new IllegalArgumentException("Tokens list cannot be null or empty");
            }

            List<UUID> uuids = tokens.stream().map(UUID::fromString).collect(Collectors.toList());

            Map<String, FileMetadata> metadataMap = fileService.getMetadataByTokens(uuids);

            Map<String, Object> response = metadataMap.entrySet().stream().collect(Collectors.toMap(
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
            customLogger.error("Bad Request: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            customLogger.logCritical(e);
            return ResponseEntity.status(503).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    }

    @GetMapping("/{token}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String token) {
        try {
            File file = fileService.getFile(token);
            if (file == null) {
                throw new IllegalArgumentException("File not found for token: " + token);
            }

            Path path = file.toPath();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File not readable for token: " + token);
            }

            FileMetadata metadata = fileService.getMetadata(UUID.fromString(token));

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .header("X-Filename", file.getName())
                    .header("X-Filesize", String.valueOf(file.length()))
                    .header("X-CreateTime", metadata.getCreateTime().toInstant().toString())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            customLogger.error("Bad Request: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            customLogger.logCritical(e);
            return ResponseEntity.status(503).body(null);
        }
    }


    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID token) {
        fileService.deleteFileByToken(token);
        return ResponseEntity.noContent().build();
    }
}
