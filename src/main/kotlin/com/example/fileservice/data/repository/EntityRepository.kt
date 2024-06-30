package com.example.fileservice.data.repository

import com.example.fileservice.data.entities.Entity
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface EntityRepository : MongoRepository<Entity, UUID> {
    fun findByTokenIn(token: Collection<UUID>): List<Entity>
}
