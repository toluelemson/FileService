package com.example.fileservice.controller

import com.example.fileservice.dto.FileUploadRequest
import com.example.fileservice.controller.exception.InternalException
import com.example.fileservice.controller.exception.NotFoundException
import com.example.fileservice.rest.ErrorMessage
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
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/file")
@Validated
class FileController(
    private val fileService: FileService,
    private val objectMapper: ObjectMapper
) {

    @PostMapping("/upload")
    fun uploadFile(@Valid @ModelAttribute request: FileUploadRequest): ResponseEntity<Map<String, String>> {
        return try {
            val metaMap: Map<String, Any> = objectMapper.readValue(request.meta, object : TypeReference<Map<String, Any>>() {})
            val token = fileService.uploadFile(request.name, request.contentType, metaMap, request.source, request.expireTime, request.content)

            val response = mapOf("token" to token)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IOException) {
            throw IllegalArgumentException("Invalid meta data: ${e.message}", e)
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
            if (tokens.isNullOrEmpty()) {
                throw IllegalArgumentException("Tokens list cannot be null or empty")
            }

            val uuids = tokens.map { UUID.fromString(it) }
            val metadataMap = fileService.getMetadataByTokens(uuids)

            val response = metadataMap.entries.associate {
                it.key to mapOf(
                    "token" to it.key,
                    "filename" to it.value.name,
                    "size" to it.value.size,
                    "contentType" to it.value.contentType,
                    "createTime" to it.value.createTime.toInstant().toString(),
                    "meta" to it.value.meta
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
