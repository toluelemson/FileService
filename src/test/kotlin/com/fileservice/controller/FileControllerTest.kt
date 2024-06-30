package com.fileservice.controller

import com.example.fileservice.FileServiceApplication
import com.example.fileservice.controller.FileController
import com.example.fileservice.data.dto.FileUploadRequest
import com.example.fileservice.service.FileService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*

@ExtendWith(SpringExtension::class)
@WebMvcTest(FileController::class)
@ContextConfiguration(classes = [FileServiceApplication::class]) // Specify your main configuration class here
@ComponentScan(basePackages = ["com.example.fileservice"])
class FileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var fileUploadRequest: FileUploadRequest
    private lateinit var expireTime: Date

    @BeforeEach
    fun setup() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        expireTime = dateFormat.parse("2024-12-31T23:59:59Z")
        val content = "Test content"
        val multipartFile: MultipartFile = MockMultipartFile(
            "file",
            "testFile.txt",
            "text/plain",
            ByteArrayInputStream(content.toByteArray())
        )
        fileUploadRequest = FileUploadRequest(
            name = "testFile",
            contentType = "text/plain",
            meta = """{"key": "value"}""",
            source = "testSource",
            expireTime = expireTime.toString(),
            content = multipartFile
        )
    }

    @Test
    fun `uploadFile should return CREATED status and token in response`() {
        val token = "testToken"

        // Prepare the meta map before the when block
        val metaMap: Map<String, Any> = objectMapper.readValue(
            fileUploadRequest.meta,
            object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})

        `when`(
            fileUploadRequest.name?.let {
                fileUploadRequest.source?.let { it1 ->
                    fileService.uploadFile(
                        it,
                        fileUploadRequest.contentType,
                        metaMap,
                        it1,
                        fileUploadRequest.expireTime.toString(),
                        fileUploadRequest.content
                    )
                }
            }
        ).thenReturn(token)

        val multipartFile = MockMultipartFile("content", "testFile.txt", "text/plain", "Test content".toByteArray())

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/file/upload")
                .file(multipartFile)
                .param("name", fileUploadRequest.name)
                .param("contentType", fileUploadRequest.contentType)
                .param("meta", fileUploadRequest.meta)
                .param("source", fileUploadRequest.source)
                .param("expireTime", fileUploadRequest.expireTime)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
    }


    @Test
    fun `uploadFile should return BAD REQUEST status for invalid meta`() {
        val invalidMeta = """{"key": value"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/file/upload")
                .file(MockMultipartFile("content", "testFile.txt", "text/plain", "Test content".toByteArray()))
                .param("name", fileUploadRequest.name)
                .param("contentType", fileUploadRequest.contentType)
                .param("meta", invalidMeta)
                .param("source", fileUploadRequest.source)
                .param("expireTime", fileUploadRequest.expireTime)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
