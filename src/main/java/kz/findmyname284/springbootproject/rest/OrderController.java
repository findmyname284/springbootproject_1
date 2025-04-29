package kz.findmyname284.springbootproject.rest;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.findmyname284.springbootproject.dto.CheckoutDTO;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.service.OrderService;
import kz.findmyname284.springbootproject.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
        }
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());

        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
        }

        User user = userOptional.get();

        Long id = orderService.processOrder(dto, user);

        if (id == -1L) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Order processing failed"));
        }

        return ResponseEntity.ok()
                .body(Collections.singletonMap("id", id));
    }
}
