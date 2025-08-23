package com.codvortex.api.EmployeeApis;

import com.codvortex.dto.EmployeeDTOs.UserSummaryDto;
import com.codvortex.service.EmployeeService.EmployeeUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employee/users")
public class EmployeeUserResource {

    @Autowired
    private EmployeeUsersService employeeUsersService;

    @GetMapping("/summaries")
    public List<UserSummaryDto> getUserSummaries(@RequestHeader("Authorization") String authHeader) {
        return employeeUsersService.getUserSummaries(authHeader);
    }


}
