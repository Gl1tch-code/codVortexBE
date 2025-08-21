package com.codvortex.dtoMappers;

import com.codvortex.domain.Product;
import com.codvortex.dto.AdminDTOs.ProductDetailsDTO;
import com.codvortex.dto.ProductDropDTO;
import com.codvortex.dto.ProductPageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductPageDTO toDTO(Product product);

    ProductDropDTO toDropDTO(Product product);

    @Mapping(target = "country", source = "country.id")
    ProductDetailsDTO toDetailsDTO(Product product);

    List<ProductPageDTO> toListDTO(List<Product> products);
}
