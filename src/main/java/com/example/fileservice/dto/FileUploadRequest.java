package com.example.fileservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Getter
@Setter
public class FileUploadRequest {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Content type cannot be blank")
    private String contentType;

    @NotBlank(message = "Meta cannot be blank")
    private String meta;

    @NotBlank(message = "Source cannot be blank")
    private String source;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date expireTime;

    @NotNull(message = "File content cannot be null")
    private MultipartFile content;
}
