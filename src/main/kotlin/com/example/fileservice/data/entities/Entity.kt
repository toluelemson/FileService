package com.example.fileservice.data.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

@Document(collection = "files")
data class Entity(
    @Id
    val id: UUID,
    val name: String,
    val contentType: String,
    val size: Long,
    val createTime: Date,
    val meta: Map<String, Any>,
    val source: String,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val expireTime: String,
    val path: String,
    val token: UUID
)
