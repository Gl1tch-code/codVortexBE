package com.codvortex.dto.AdminDTOs;

import com.codvortex.dto.SourcingDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserDetailsDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean isActive;
    private Boolean isAccountManagerAssigned;
    private List<SourcingDTO> sourcing;
    private List<Long> filesIds;
}
