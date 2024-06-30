package com.example.fileservice.controller

import com.example.fileservice.dto.FileUploadRequest
import com.example.fileservice.controller.exception.InternalException
import com.example.fileservice.controller.exception.NotFoundException
import com.example.fileservice.data.dto.FileUploadRequest
import com.example.fileservice.rest.ErrorMessage
import com.example.fileservice.service.FileService
import com.fasterxml.jackson.core.type.TypeReference
import com.example.fileservice.data.entities.Entity
import com.example.fileservice.service.FileService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/api/file")
class FileController(
    private val objectMapper: ObjectMapper,
    private val fileService: FileService
) {

    @PostMapping("/upload")
    fun uploadFile(@Valid @ModelAttribute request: FileUploadRequest): ResponseEntity<Map<String, String>> {
        return try {
            val metaMap: Map<String, Any> =
                objectMapper.readValue(request.meta, object : TypeReference<Map<String, Any>>() {})

            val token = fileService.uploadFile(
                request.name,
                request.contentType,
                metaMap,
                request.source,
                request.expireTime!!,
                request.content
            )

            val response = mapOf("token" to token)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IOException) {
            throw IllegalArgumentException("${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw InternalException("Internal server error: ${e.message}", e)
        }
    }

    @PostMapping("/metas")
    fun getFileMetadata(@RequestBody request: Map<String, List<String>>): ResponseEntity<Map<String, Any>> {
        return try {
            val tokens = request["tokens"]
                ?: throw IllegalArgumentException("Tokens list cannot be null or empty")

            if (tokens.isEmpty()) {
                throw IllegalArgumentException("Tokens list cannot be null or empty")
            }

            val uuids = tokens.map { UUID.fromString(it) }
            val metadataMap = fileService.getMetadataByTokens(uuids)

            val response = metadataMap.entries.associate { (key, value) ->
                key to mapOf(
                    "token" to key,
                    "filename" to value.name,
                    "size" to value.size,
                    "contentType" to value.contentType,
                    "createTime" to value.createTime.toInstant().toString(),
                    "meta" to value.meta
                )
            }
            ResponseEntity.ok(mapOf("files" to response))
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw InternalException("Internal server error: ${e.message}", e)
        }
    }

    @GetMapping("/{token}")
    fun downloadFile(@PathVariable token: String): ResponseEntity<*> {
        return try {
            val file = fileService.getFile(token)
            file ?: throw NotFoundException("File not found for token: $token")

            val path = file.toPath()
            val resource: Resource = UrlResource(path.toUri())
            if (!resource.exists() || !resource.isReadable) {
                throw NotFoundException("File not readable for token: $token")
            }

            val metadata = fileService.getMetadata(UUID.fromString(token))
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.name}\"")
                .header("X-Filename", file.name)
                .header("X-Filesize", file.length().toString())
                .header("X-CreateTime", metadata.createTime.toInstant().toString())
                .body(resource)
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorMessage(e.message))
        } catch (e: Exception) {
            throw InternalException("Internal server error: ${e.message}", e)
        }
    }

    @DeleteMapping("/{token}")
    fun deleteFile(@PathVariable token: UUID): ResponseEntity<ErrorMessage> {
        return try {
            val isDeleted = fileService.deleteFileByToken(token)
            if (!isDeleted) {
                throw NotFoundException("File not found for token: $token")
            }
            ResponseEntity.noContent().build()
        } catch (e: NotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorMessage(e.message))
        } catch (e: Exception) {
            throw InternalException("Internal server error: ${e.message}", e)
        }
    }
}
