package com.codvortex.dtoMappers;

import com.codvortex.domain.Sourcing;
import com.codvortex.dto.AdminDTOs.SourcingDetailsDTO;
import com.codvortex.dto.SourcingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SourcingMapper {
    SourcingDTO toDTO(Sourcing sourcing);

    @Mapping(target = "country", source = "country.id")
    SourcingDetailsDTO toDetailsDTO(Sourcing sourcing);

    List<SourcingDTO> toListDTO(List<Sourcing> sourcings);
}
