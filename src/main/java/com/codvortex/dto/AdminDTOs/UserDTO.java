package com.codvortex.dto.AdminDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean isActive;
    private Boolean isAccountManagerAssigned;
    private BigDecimal balance;
}
