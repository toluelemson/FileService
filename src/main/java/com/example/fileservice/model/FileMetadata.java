package com.example.fileservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Document(collection = "files")
@Getter
@AllArgsConstructor
public class FileMetadata {

    @Id
    private UUID id;
    private final String name;
    private final String contentType;
    private final long size;
    private final Date createTime;
    private final Map<String, Object> meta;
    private final String source;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final Date expireTime;
    private final String path;
    private final UUID token;
}
