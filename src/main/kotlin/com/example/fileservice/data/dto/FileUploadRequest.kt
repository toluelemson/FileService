package com.example.fileservice.data.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.multipart.MultipartFile

data class FileUploadRequest(

    @field:NotBlank(message = "Name cannot be blank") val name: String,

    @field:NotBlank(message = "Content type cannot be blank") val contentType: String,

    @field:NotBlank(message = "Meta cannot be blank") val meta: String,

    @field:NotBlank(message = "Source cannot be blank") val source: String,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) val expireTime: String?,

    @field:NotNull(message = "File content cannot be null") val content: MultipartFile?
)
