package com.codvortex.dto.AdminDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SourcingDetailsDTO {
    private String country;
    private List<ProductDetailsDTO> products;
}
