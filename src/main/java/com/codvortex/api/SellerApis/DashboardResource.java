package com.codvortex.api.SellerApis;

import com.codvortex.dto.DashboardOrdersSummaryDTO;
import com.codvortex.service.SellerServices.dashboard.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/dashboard")
public class DashboardResource {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(dashboardService.getBalance(authHeader));
    }

    @GetMapping("/all-delivered")
    public ResponseEntity<Integer> getPerformance(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(dashboardService.getUserPerf(authHeader));
    }

    @GetMapping("/orders")
    public ResponseEntity<DashboardOrdersSummaryDTO> getOrders(@RequestHeader("Authorization") String authHeader, @RequestParam String startDate, @RequestParam String endDate, @RequestParam String country) {
        Instant startDateinstant = Instant.parse(startDate);
        LocalDateTime startDateinstantlocalDateTime = LocalDateTime.ofInstant(startDateinstant, ZoneId.systemDefault());

        Instant endDateinstant = Instant.parse(endDate);
        LocalDateTime endDateinstantlocalDateTime = LocalDateTime.ofInstant(endDateinstant, ZoneId.systemDefault());

        return ResponseEntity.ok(dashboardService.getOrdersSummary(authHeader, startDateinstantlocalDateTime, endDateinstantlocalDateTime, country));
    }

}
