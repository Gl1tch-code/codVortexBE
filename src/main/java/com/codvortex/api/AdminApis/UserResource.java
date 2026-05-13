package com.codvortex.api.AdminApis;

import com.codvortex.dto.AdminDTOs.SourcingDetailsDTO;
import com.codvortex.dto.AdminDTOs.UserDTO;
import com.codvortex.dto.AdminDTOs.UserDetailsDTO;
import com.codvortex.service.AdminServices.users.AdminUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class UserResource {

    @Autowired
    private AdminUsersService adminUsersService;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(@RequestParam String q, Pageable pageable, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(adminUsersService.getUsers(q, pageable, authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUserDetails(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(adminUsersService.getUserDetails(id, authHeader));
    }

    @GetMapping("/sourcing/{sourcingId}")
    public ResponseEntity<SourcingDetailsDTO> getUserSourcingDetails(@PathVariable Long sourcingId, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(adminUsersService.getUserSourcingDetails(sourcingId, authHeader));
    }

    @PostMapping("/{id}/activation")
    public ResponseEntity<Void> activateUser(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        adminUsersService.toggleUserActivation(id, authHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/account-manager-assigned")
    public ResponseEntity<Void> toggleIsAccountManagerAssigned(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        adminUsersService.toggleIsAccountManagerAssigned(id, authHeader);
        return ResponseEntity.ok().build();
    }

}
