package com.example.fileservice.controller;

import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.Matchers.is;
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

    @Test
    void testGetFileMetadata() throws Exception {
        UUID mockId1 = UUID.randomUUID();
        UUID mockId2 = UUID.randomUUID();
        String token1 = mockId1.toString();
        String token2 = mockId2.toString();

        FileMetadata metadata1 = new FileMetadata(mockId1, "ExampleName", "application/pdf", 51510, new Date(), Map.of("description", "Sample file", "tags", Arrays.asList("tag1", "tag2")), "source", new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), "uploads/mockToken1_test.pdf", mockId1);

        FileMetadata metadata2 = new FileMetadata(mockId2, "ExampleName", "application/pdf", 51510, new Date(), Map.of("description", "Sample file", "tags", Arrays.asList("tag1", "tag2")), "source", new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), "uploads/mockToken2_test.pdf", mockId2);

        when(fileService.getMetadataByTokens(any())).thenReturn(Map.of(token1, metadata1, token2, metadata2));

        Map<String, List<String>> requestBody = Map.of("tokens", Arrays.asList(token1, token2));

        String requestJson = objectMapper.writeValueAsString(requestBody);
        System.out.println("Request JSON: " + requestJson);

        String responseJson = mockMvc.perform(post("/api/file/metas").contentType("application/json").content(requestJson)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        System.out.println("Response JSON: " + responseJson);

        mockMvc.perform(post("/api/file/metas").contentType("application/json").content(objectMapper.writeValueAsString(requestBody))).andExpect(status().isOk()).andExpect(jsonPath("$.token." + token1 + ".filename", is("ExampleName"))).andExpect(jsonPath("$.token." + token1 + ".size", is(51510))).andExpect(jsonPath("$.token." + token1 + ".contentType", is("application/pdf"))).andExpect(jsonPath("$.token." + token1 + ".meta.description", is("Sample file"))).andExpect(jsonPath("$.token." + token1 + ".meta.tags[0]", is("tag1"))).andExpect(jsonPath("$.token." + token1 + ".meta.tags[1]", is("tag2"))).andExpect(jsonPath("$.token." + token2 + ".filename", is("ExampleName"))).andExpect(jsonPath("$.token." + token2 + ".size", is(51510))).andExpect(jsonPath("$.token." + token2 + ".contentType", is("application/pdf"))).andExpect(jsonPath("$.token." + token2 + ".meta.description", is("Sample file"))).andExpect(jsonPath("$.token." + token2 + ".meta.tags[0]", is("tag1"))).andExpect(jsonPath("$.token." + token2 + ".meta.tags[1]", is("tag2")));
    }

    @Test
    void testDownloadFile() throws Exception {
        String token = UUID.randomUUID().toString();
        File file = new File("uploads/mockToken_test.pdf");
        Path path = Paths.get(file.getAbsolutePath());

        Files.createDirectories(path.getParent());
        Files.createFile(path);

        Files.write(path, "Test content".getBytes());

        FileMetadata metadata = new FileMetadata(
                UUID.fromString(token),
                file.getName(),
                "application/octet-stream",
                file.length(),
                new Date(),
                Map.of("description", "Sample file", "tags", Arrays.asList("tag1", "tag2")),
                "source",
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24),
                path.toString(),
                UUID.fromString(token)
        );

        when(fileService.getFile(token)).thenReturn(file);
        when(fileService.getMetadata(UUID.fromString(token))).thenReturn(metadata);

        mockMvc.perform(get("/api/file/" + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\""))
                .andExpect(header().string("X-Filename", file.getName()))
                .andExpect(header().string("X-Filesize", String.valueOf(file.length())))
                .andExpect(header().string("X-CreateTime", metadata.getCreateTime().toInstant().toString()))
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(content().bytes(Files.readAllBytes(path)));

        Files.deleteIfExists(path);
    }

    @Test
    void testDeleteFile() throws Exception {
        UUID token = UUID.randomUUID();

        mockMvc.perform(delete("/api/file/" + token)).andExpect(status().isNoContent());
    }
}
