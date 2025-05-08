package kz.findmyname284.springbootproject.rest;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.findmyname284.springbootproject.dto.RegisterDTO;
import kz.findmyname284.springbootproject.dto.UpdateUserRequest;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AlreadyExistsException;
import kz.findmyname284.springbootproject.exception.AuthorizationException;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRestController {
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.ADMIN);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().body(userService.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<?> addUser(@RequestBody RegisterDTO user, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.ADMIN);
            userService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("success", "Пользователь создал"));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.ADMIN);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.ADMIN);
            User user = userService.updateUser(id, request);
            return ResponseEntity.ok(user);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.ADMIN);
            userService.deleteById(id);
            return ResponseEntity.ok().body(Collections.singletonMap("success", "Пользователь успешно удален"));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Пользователь не найден"));
        }
    }

    @GetMapping("roles")
    public ResponseEntity<?> getRoles(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.ADMIN);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(UserRole.values());
    }
}