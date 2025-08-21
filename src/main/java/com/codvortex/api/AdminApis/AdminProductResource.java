package com.codvortex.api.AdminApis;

import com.codvortex.service.AdminServices.product.AdminProductService;
import com.codvortex.service.SellerServices.file.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/product")
public class AdminProductResource {

    @Autowired
    private AdminProductService productService;
    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/name/{id}")
    public ResponseEntity<Void> updateName(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        productService.updateName(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/price/{id}")
    public ResponseEntity<Void> updatePrice(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        productService.updatePrice(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quantity/{id}")
    public ResponseEntity<Void> updateQuantity(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        productService.updateQuantity(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/available-stock/{id}")
    public ResponseEntity<Void> updateIsAvailableStock(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        productService.updateIsAvailableStock(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/country/{id}")
    public ResponseEntity<Void> updateCountry(@PathVariable Long id, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        productService.updateCountry(id, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/sourcing/{sourcingId}/quantity")
    public ResponseEntity<Void> updateSourcingQuantity(@PathVariable Long id, @PathVariable Long sourcingId, @RequestParam String value, @RequestHeader("Authorization") String authHeader) {
        productService.updateSourcingQuantity(id, sourcingId, value, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/img-upload")
    public ResponseEntity<String> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String fileLink = fileUploadService.storeProductImage(id, file);
            return ResponseEntity.ok(fileLink);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
