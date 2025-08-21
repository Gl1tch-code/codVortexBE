package com.codvortex.dto.AdminDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CountryDTO {
    private Long id;
    private String name;
    private String key;
}
