package com.codvortex.api;

import com.codvortex.dto.AdminDTOs.CountryDTO;
import com.codvortex.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/global")
public class GlobalResource {
    @Autowired
    private CountryRepository countryRepository;

    @GetMapping("/health")
    public ResponseEntity<Boolean> healthCheck() {
        return ResponseEntity.ok(true);
    }


    @GetMapping("/countries")
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        return ResponseEntity.ok(
                countryRepository.findAll().stream().map(c -> CountryDTO.builder()
                        .key(c.getKey())
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                    .toList()
        );
    }

}
