package com.codvortex.api.AdminApis;

import com.codvortex.service.AdminServices.sourcing.AdminSourcingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sourcing")
public class AdminSourcingResource {

    @Autowired
    private AdminSourcingService adminSourcingService;

    @PostMapping("/status/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        adminSourcingService.changeStatus(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payment-status/{id}")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        adminSourcingService.changePaymentStatus(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/total-price/{id}")
    public ResponseEntity<Void> updateTotalPrice(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        adminSourcingService.changeTotalPrice(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shipping-fees/{id}")
    public ResponseEntity<Void> updateShippingFees(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        adminSourcingService.changeShippingFees(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invoice/{id}")
    public ResponseEntity<Void> updateInvoice(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        adminSourcingService.changeInvoice(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payment-invoice/{id}")
    public ResponseEntity<Void> updatePaymentInvoice(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        adminSourcingService.changePaymentInvoice(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/country/{id}")
    public ResponseEntity<Void> updateCountry(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        adminSourcingService.changeCountry(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

}
