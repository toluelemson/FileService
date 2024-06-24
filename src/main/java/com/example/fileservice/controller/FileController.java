package com.example.fileservice.controller;

import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final ObjectMapper objectMapper;

    public FileController(FileService fileService, ObjectMapper objectMapper) {
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }


    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("name") String name,
            @RequestParam("contentType") String contentType,
            @RequestParam("meta") String meta,
            @RequestParam("source") String source,
            @RequestParam(value = "expireTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date expireTime,
            @RequestParam("content") MultipartFile file) {

        try {
            Map<String, Object> metaMap = objectMapper.readValue(meta, Map.class);
            String token = fileService.uploadFile(name, contentType, metaMap, source, expireTime, file);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            logger.info("File uploaded successfully: {}", name);
            return ResponseEntity.status(201).body(response);

        } catch (IOException e) {
            logger.error("Failed to upload file: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    @PostMapping("/metas")
    public ResponseEntity<Map<String, Object>> getFileMetadata(@RequestBody Map<String, List<String>> request) {
        List<String> tokens = request.get("tokens");
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

        return ResponseEntity.ok(Map.of("token", response));

    }

    @GetMapping("/{token}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String token) {
        File file = fileService.getFile(token);
        if (file == null) {
            return ResponseEntity.status(404).body(null);
        }

        Path path = file.toPath();
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(404).body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }

        FileMetadata metadata = fileService.getMetadata(UUID.fromString(token));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header("X-Filename", file.getName())
                .header("X-Filesize", String.valueOf(file.length()))
                .header("X-CreateTime", metadata.getCreateTime().toInstant().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
