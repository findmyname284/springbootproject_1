package kz.findmyname284.springbootproject.rest;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.findmyname284.springbootproject.dto.SupplierDTO;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AlreadyExistsException;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.service.SupplierService;
import kz.findmyname284.springbootproject.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {
    private final UserService userService;
    private final SupplierService supplierService;

    @PostMapping("/register")
    @CrossOrigin
    public ResponseEntity<?> register(@Valid @RequestBody SupplierDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
        }
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());

        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
        }

        User user = userOptional.get();
        try {
            supplierService.register(dto, user);
            UserRole[] roles = {UserRole.MANAGER, UserRole.ADMIN};
            for (UserRole role : roles) {
                if (user.getRole() == role) {
                    return ResponseEntity.ok().build();
                }
            }
            userService.updateRole(user, UserRole.SUPPLIER);
            return ResponseEntity.ok().build();
        } catch (AlreadyExistsException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
