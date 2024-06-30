package com.example.fileservice.service

import com.example.fileservice.data.entities.Entity
import com.example.fileservice.data.repository.EntityRepository
import com.example.fileservice.controller.exception.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.*

@Service
open class FileService @Autowired constructor(
    private val entityRepository: EntityRepository
) {

    private val storageLocation: Path = Paths.get("storage").toAbsolutePath().normalize()

    init {
        try {
            Files.createDirectories(this.storageLocation)
        } catch (e: IOException) {
            throw RuntimeException("Could not create storage directory", e)
        }
    }

    @Transactional
    @Throws(IOException::class)
    open fun uploadFile(
        name: String,
        contentType: String?,
        meta: Map<String, Any>,
        source: String,
        expireTime: String,
        file: MultipartFile?
    ): String {
        val token = UUID.randomUUID().toString()
        val targetLocation = storageLocation.resolve(token)

        try {
            file?.inputStream?.use { inputStream ->
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: IOException) {
            throw IOException("Failed to store file", e)
        }

        val size = Files.size(targetLocation)
        val createTime = Date()

        val metadata = Entity(
            id = UUID.randomUUID(),
            name = name,
            contentType = contentType ?: "application/octet-stream",
            size = size,
            createTime = createTime,
            meta = meta,
            source = source,
            expireTime = expireTime,
            path = targetLocation.toString(),
            token = UUID.fromString(token)
        )

        entityRepository.save(metadata)
        return token
    }

    @Transactional(readOnly = true)
    open fun getMetadata(token: UUID): Entity {
        return entityRepository.findByTokenIn(listOf(token))
            .firstOrNull() ?: throw NotFoundException("File metadata not found for token: $token")
    }

    @Transactional(readOnly = true)
    open fun getFile(token: String): File {
        val metadata = getMetadata(UUID.fromString(token))
        val file = File(metadata.path)
        if (!file.exists()) {
            throw NotFoundException("File does not exist for token: $token")
        }
        if (!file.canRead()) {
            throw NotFoundException("File cannot be read for token: $token")
        }
        return file
    }

    @Transactional(readOnly = true)
    open fun getMetadataByTokens(tokens: List<UUID>): Map<String, Entity> {
        val metadataList = entityRepository.findByTokenIn(tokens)
        return metadataList.associateBy { it.token.toString() }
    }

    @Transactional
    open fun deleteFileByToken(token: UUID): Boolean {
        val metadata = getMetadata(token)
        entityRepository.deleteById(metadata.id)

        return try {
            Files.deleteIfExists(Paths.get(metadata.path))
            true
        } catch (e: IOException) {
            throw RuntimeException("Failed to delete file", e)
        }
    }
}
