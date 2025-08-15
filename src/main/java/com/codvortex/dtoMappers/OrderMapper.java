package com.codvortex.dtoMappers;

import com.codvortex.domain.Order;
import com.codvortex.dto.OrderDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDTO toDTO(Order order);

    List<OrderDTO> toListDTO(List<Order> orders);
}
