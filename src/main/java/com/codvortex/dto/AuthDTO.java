package com.codvortex.dto;

import com.codvortex.utils.RoleEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@Builder
public class AuthDTO {
    private String username;
    private String email;
    private BigInteger balance;
    private RoleEnum role;
    private String rib;
    private String bankName;
    private String token;
}
