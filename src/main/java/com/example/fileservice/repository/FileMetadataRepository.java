package com.example.fileservice.repository;

import com.example.fileservice.model.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FileMetadataRepository extends MongoRepository<FileMetadata, UUID> {
    List<FileMetadata> findByTokenIn(Collection<UUID> token);
}
