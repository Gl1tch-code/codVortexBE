package com.codvortex.api.AdminApis;

import com.codvortex.dto.DashboardOrdersSummaryDTO;
import com.codvortex.service.SellerServices.dashboard.DashboardService;
import com.codvortex.utils.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardResource {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/orders")
    public ResponseEntity<DashboardOrdersSummaryDTO> getOrders(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String country,
            @RequestParam(required = false) Long productId) { // Added optional param

        Instant startDateinstant = Instant.parse(startDate);
        LocalDateTime start = LocalDateTime.ofInstant(startDateinstant, ZoneId.systemDefault());

        Instant endDateinstant = Instant.parse(endDate);
        LocalDateTime end = LocalDateTime.ofInstant(endDateinstant, ZoneId.systemDefault());

        return ResponseEntity.ok(dashboardService.getOrdersSummary(authHeader, start, end, country, productId, RoleEnum.ADMIN));
    }

}
