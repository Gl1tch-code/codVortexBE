package com.codvortex.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserBillings {
    private Long id;
    private String rib;
    private String bankName;

}
