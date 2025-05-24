package com.codvortex.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/global")
public class GlobalResource {

    @GetMapping("/health")
    public ResponseEntity<Boolean> healthCheck() {
        return ResponseEntity.ok(true);
    }
}
