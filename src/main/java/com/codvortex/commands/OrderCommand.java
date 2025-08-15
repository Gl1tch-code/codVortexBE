package com.codvortex.commands;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCommand {
    private String address;
    private String customerName;
    private String phoneNumber;
    private int price;
    private int quantite;
    private Long product;
    private Boolean isAvailableStock;
}
