package com.codvortex.api;

import com.codvortex.commands.SourcingCommand;
import com.codvortex.dto.SourcingDTO;
import com.codvortex.service.sourcing.SourcingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sourcing")
public class SourcingResource {

    private final SourcingService sourcingService;

    public SourcingResource(SourcingService sourcingService) {
        this.sourcingService = sourcingService;
    }

    @GetMapping
    public ResponseEntity<Page<SourcingDTO>> getAll(@RequestParam String q, Pageable pageable, String country, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(sourcingService.gatAll(q, pageable, country, authHeader));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody SourcingCommand sourcingCommand, String country, @RequestHeader("Authorization") String authHeader) {
        sourcingService.create(sourcingCommand, country, authHeader);
        return ResponseEntity.ok().build();
    }
}
