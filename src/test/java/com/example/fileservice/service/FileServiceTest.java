package com.example.fileservice.service;

import com.example.fileservice.exception.ResourceNotFoundException;
import com.example.fileservice.model.FileMetadata;
import com.example.fileservice.repository.FileMetadataRepository;
import com.example.fileservice.util.CustomLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @InjectMocks
    private FileServiceHelper fileServiceHelper;

    @Mock
    private CustomLogger customLogger;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadFile_ShouldStoreFileAndReturnToken() throws IOException {
        String name = "testFile.txt";
        String contentType = "text/plain";
        Map<String, Object> meta = new HashMap<>();
        String source = "testSource";
        Date expireTime = new Date();
        MultipartFile file = new MockMultipartFile("file", name, contentType, "Hello, World!".getBytes());

        String token = fileService.uploadFile(name, contentType, meta, source, expireTime, file);

        assertNotNull(token);
        verify(customLogger).info(contains("File uploaded successfully"));
        verify(fileMetadataRepository).save(any(FileMetadata.class));

        Path targetLocation = Paths.get("storage").resolve(token);
        Files.deleteIfExists(targetLocation);
    }

    @Test
    void uploadFile_ShouldThrowIOException_WhenFileCopyFails() throws IOException {
        String name = "testFile.txt";
        String contentType = "text/plain";
        Map<String, Object> meta = new HashMap<>();
        String source = "testSource";
        Date expireTime = new Date();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getInputStream()).thenThrow(new IOException("File copy failed"));

        IOException exception = assertThrows(IOException.class, () -> fileService.uploadFile(name, contentType, meta, source, expireTime, file));

        assertEquals("Failed to store file", exception.getMessage());
        verify(customLogger).error(contains("Failed to store file"), any(IOException.class));
    }

    @Test
    void testGetMetadata_Success() {
        UUID token = UUID.randomUUID();

        String name = "example.txt";
        String contentType = "text/plain";
        long size = 1234L;
        Date createTime = new Date();
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        String source = "source";
        Date expireTime = new Date(createTime.getTime() + 10000);
        String path = "storage/" + token;

        FileMetadata metadata = new FileMetadata(UUID.randomUUID(), name, contentType, size, createTime, meta, source, expireTime, path, token);

        when(fileMetadataRepository.findByTokenIn(Collections.singletonList(token))).thenReturn(Collections.singletonList(metadata));

        FileMetadata result = fileService.getMetadata(token);

        assertNotNull(result);
        assertEquals(metadata, result);
    }

    @Test
    void testGetMetadata_NotFound() {
        UUID token = UUID.randomUUID();

        when(fileMetadataRepository.findByTokenIn(Collections.singletonList(token))).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> fileService.getMetadata(token));

        assertEquals("File metadata not found for token: " + token, exception.getMessage());
    }

    @Test
    void testGetFile_NotReadable() {
        UUID token = UUID.randomUUID();

        String name = "example.txt";
        String contentType = "text/plain";
        long size = 1234L;
        Date createTime = new Date();
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        String source = "source";
        Date expireTime = new Date(createTime.getTime() + 10000);
        String path = "storage/" + token;

        FileMetadata metadata = new FileMetadata(UUID.randomUUID(), name, contentType, size, createTime, meta, source, expireTime, path, token);

        when(fileMetadataRepository.findByTokenIn(Collections.singletonList(token))).thenReturn(Collections.singletonList(metadata));

        FileServiceHelper fileServiceHelperSpy = spy(fileServiceHelper);
        FileService fileService = new FileService(fileMetadataRepository, customLogger);

        doReturn(new File(path) {
            @Override
            public boolean exists() {
                return false;
            }
        }).when(fileServiceHelperSpy).createFile();

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> fileService.getFile(token.toString()));

        assertEquals("File not readable for token: " + token, exception.getMessage());
    }

    @Test
    void testDeleteFileByToken_Success() {
        UUID token = UUID.randomUUID();
        String name = "example.txt";
        String contentType = "text/plain";
        long size = 1234L;
        Date createTime = new Date();
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        String source = "source";
        Date expireTime = new Date(createTime.getTime() + 10000);
        String path = "storage/" + token;

        FileMetadata metadata = new FileMetadata(UUID.randomUUID(), name, contentType, size, createTime, meta, source, expireTime, path, token);

        when(fileMetadataRepository.findByTokenIn(Collections.singletonList(token))).thenReturn(Collections.singletonList(metadata));
        doNothing().when(fileMetadataRepository).deleteById(metadata.getToken());

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(Paths.get(path))).thenReturn(true);

            boolean result = fileService.deleteFileByToken(token);

            assertTrue(result);
            verify(fileMetadataRepository, times(1)).deleteById(metadata.getToken());
            verify(customLogger, times(1)).info("File deleted successfully for token: " + token);
        }
    }
}
