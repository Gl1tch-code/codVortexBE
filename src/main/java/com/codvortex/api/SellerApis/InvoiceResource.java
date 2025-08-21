package com.codvortex.api.SellerApis;

import com.codvortex.dto.InvoiceDTO;
import com.codvortex.service.SellerServices.invoice.InvoiceService;
import com.codvortex.utils.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
public class InvoiceResource {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<Page<InvoiceDTO>> getOrders(@RequestParam String q, @RequestParam(required = false) InvoiceStatus status, Pageable pageable, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(q, status, pageable, authHeader));
    }

    @PostMapping
    public ResponseEntity<Void> createInvoice(@RequestHeader("Authorization") String authHeader) {
        invoiceService.createInvoice(authHeader);
        return ResponseEntity.ok().build();
    }

}
