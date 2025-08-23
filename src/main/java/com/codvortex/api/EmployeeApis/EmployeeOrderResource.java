package com.codvortex.api.EmployeeApis;

import com.codvortex.dto.DashboardOrdersSummaryDTO;
import com.codvortex.dto.EmployeeDTOs.OrderEmployeeDTO;
import com.codvortex.dto.OrderDTO;
import com.codvortex.service.AdminServices.product.AdminProductService;
import com.codvortex.service.EmployeeService.EmployeeOrderService;
import com.codvortex.service.SellerServices.file.FileUploadService;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/employee/orders")
public class EmployeeOrderResource {

    @Autowired
    private EmployeeOrderService employeeOrderService;

    @GetMapping
    public ResponseEntity<Page<OrderEmployeeDTO>> getOrders(@RequestParam String q, OrderStatusEnum status, OrderShippinStatusEnum shippingStatus, Boolean paymentStatus, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable, Long country, Long userId, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(employeeOrderService.getAllOrdersAsEmployee(q, status, shippingStatus, paymentStatus, startDate, endDate, pageable, country, userId, authHeader));
    }

    @PostMapping("/address/{id}")
    public ResponseEntity<Void> updateAddress(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        employeeOrderService.updateAddress(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quantity/{id}")
    public ResponseEntity<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer value, @RequestHeader("Authorization") String authHeader) {
        employeeOrderService.updateQuantity(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/status/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        employeeOrderService.updateStatus(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shipping-status/{id}")
    public ResponseEntity<Void> updateShippingStatus(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        employeeOrderService.updateShippingStatus(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/message/{id}")
    public ResponseEntity<Void> updateMessage(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        employeeOrderService.updateMessage(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/country/{id}")
    public ResponseEntity<Void> updateCountry(@PathVariable Long id, @RequestParam Long value, @RequestHeader("Authorization") String authHeader) {
        employeeOrderService.updateCountry(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

}
