package com.example.fileservice;

import com.example.fileservice.controller.FileController;
import com.example.fileservice.service.FileService;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FileServiceApplicationTests {

    @Autowired
    private FileController fileController;

    @Autowired
    private FileService fileService;

    @Test
    void contextLoads() {
    }

    @Test
    void testFileControllerLoaded() {
        assertThat(fileController).isNotNull();
    }

    @Test
    void testFileServiceLoaded() {
        assertThat(fileService).isNotNull();
    }
}
