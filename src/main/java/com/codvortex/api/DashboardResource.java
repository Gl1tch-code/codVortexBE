package com.codvortex.api;

 import com.codvortex.service.dashboard.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/dashboard")
public class DashboardResource {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(dashboardService.getBalance(authHeader));
    }
}
