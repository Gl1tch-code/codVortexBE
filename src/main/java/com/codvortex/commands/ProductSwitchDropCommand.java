package com.codvortex.commands;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSwitchDropCommand {
    private Integer quantity;
    private BigDecimal price;
}
