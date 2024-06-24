package com.example.fileservice.controller;

import com.example.fileservice.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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
}
