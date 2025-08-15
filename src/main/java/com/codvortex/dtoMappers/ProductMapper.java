package com.codvortex.dtoMappers;

import com.codvortex.domain.Product;
import com.codvortex.dto.ProductDropDTO;
import com.codvortex.dto.ProductPageDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductPageDTO toDTO(Product product);
    ProductDropDTO toDropDTO(Product product);

    List<ProductPageDTO> toListDTO(List<Product> products);
}
