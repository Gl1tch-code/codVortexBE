package com.codvortex.dtoMappers;

import com.codvortex.domain.Order;
import com.codvortex.dto.EmployeeDTOs.OrderEmployeeDTO;
import com.codvortex.dto.OrderDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDTO toDTO(Order order);

    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productImage", source = "product.img")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    OrderEmployeeDTO toEmployeeDTO(Order order);

    List<OrderDTO> toListDTO(List<Order> orders);
}
