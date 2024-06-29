package com.example.fileservice.data.repository;

import com.example.fileservice.data.entities.Entity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EntityRepository extends MongoRepository<Entity, UUID> {
    List<Entity> findByTokenIn(Collection<UUID> token);
}
