package com.codvortex.api;

import com.codvortex.commands.OrderCommand;
import com.codvortex.dto.OrderDTO;
import com.codvortex.service.order.OrderService;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/orders")
public class OrdersResource {

    @Autowired
    private OrderService orderService;


    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getOrders(@RequestParam String q, OrderStatusEnum status, OrderShippinStatusEnum shippingStatus, Pageable pageable, String country, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(orderService.getAllOrders(q, status, shippingStatus, pageable, country, authHeader));
    }

    @PostMapping
    public ResponseEntity<ResponseEntity<Void>> createOrder(@RequestBody OrderCommand orderCommand, @RequestParam("country") String country, @RequestHeader("Authorization") String authHeader
    ) {
        orderService.saveOrder(orderCommand, country, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import/csv")
    public ResponseEntity<String> importOrdersCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("country") String country,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            orderService.importFromCsv(file, authHeader, country);
            return ResponseEntity.ok("CSV imported successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Import failed: " + e.getMessage());
        }
    }

}
