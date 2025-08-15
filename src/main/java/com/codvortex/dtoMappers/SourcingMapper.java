package com.codvortex.dtoMappers;

import com.codvortex.domain.Sourcing;
import com.codvortex.dto.SourcingDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SourcingMapper {

    SourcingDTO toDTO(Sourcing sourcing);
    List<SourcingDTO> toListDTO(List<Sourcing> sourcings);
}
