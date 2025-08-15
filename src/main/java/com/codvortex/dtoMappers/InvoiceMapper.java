package com.codvortex.dtoMappers;

import com.codvortex.domain.Invoice;
import com.codvortex.dto.InvoiceDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {
    InvoiceDTO toDTO(Invoice invoice);

    List<InvoiceDTO> toListDTO(List<Invoice> invoices);
}
