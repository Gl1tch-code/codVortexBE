package com.codvortex.commands;

import com.codvortex.dto.ProductDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SourcingCommand {
    private String title;
    private String message;
    private List<SourcingProductCommand> products;
}
