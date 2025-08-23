package com.codvortex.service.EmployeeService;

import com.codvortex.configuration.JwtTokenService;
import com.codvortex.dto.EmployeeDTOs.UserSummaryDto;
import com.codvortex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeUsersService {
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    public List<UserSummaryDto> getUserSummaries(String token) {
        userRepository.findByUsernameAndEmployeeOrAdmin(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        return userRepository.findAllSummaries();
    }

}
