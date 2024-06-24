package com.example.fileservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Document(collection = "files")
public class FileMetadata {

    @Id
    private UUID id;
    private final String name;
    private final String contentType;
    private final long size;
    private final Date createTime;
    private final Map<String, Object> meta;
    private final String source;
    private final Date expireTime;
    private final String path;
    private final UUID token;

    public FileMetadata(UUID id, String name, String contentType, long size, Date createTime, Map<String, Object> meta, String source, Date expireTime, String path, UUID token) {
        this.id = id;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.createTime = createTime;
        this.meta = meta;
        this.source = source;
        this.expireTime = expireTime;
        this.path = path;
        this.token = token;
    }

    public UUID getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public String getPath() {
        return path;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public String getSource() {
        return source;
    }
}
