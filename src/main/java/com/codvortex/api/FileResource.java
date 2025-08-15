package com.codvortex.api;

import com.codvortex.domain.File;
import com.codvortex.service.file.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileResource {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File storedFile = fileUploadService.storeFile(file);
            return ResponseEntity.ok(storedFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/upload-identity")
    public ResponseEntity<Object> uploadIdentity(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) {
        try {
            File storedFile = fileUploadService.storeIdentity(file, authHeader);
            return ResponseEntity.ok(storedFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> getFile(@PathVariable Long id) {
        File fileData = fileUploadService.getFile(id);
        if (fileData != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileData.getName() + "\"")
                    .body(new ByteArrayResource(fileData.getData()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
