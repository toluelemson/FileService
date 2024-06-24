package com.example.fileservice.controller;

import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void testUploadFile() throws Exception {
        String mockToken = UUID.randomUUID().toString();
        MockMultipartFile file = new MockMultipartFile("content", "test.txt", "text/plain", "Spring Framework".getBytes());
        String meta = "{\"creatorEmployeeId\": 1}";
        String source = "testSource";
        String expireTime = "2024-12-31T23:59:59.000+00:00";

        when(fileService.uploadFile(any(), any(), any(), any(), any(), any())).thenReturn(mockToken);

        mockMvc.perform(multipart("/api/file/upload").file(file).param("name", "test.txt").param("contentType", "text/plain").param("meta", meta).param("source", source).param("expireTime", expireTime))
                .andExpect(status().isCreated());
    }
}
