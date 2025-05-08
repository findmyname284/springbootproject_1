package kz.findmyname284.springbootproject.rest;

import java.math.BigDecimal;
import java.util.Collections;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kz.findmyname284.springbootproject.dto.LoginDTO;
import kz.findmyname284.springbootproject.dto.RegisterDTO;
import kz.findmyname284.springbootproject.exception.AlreadyExistsException;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.utils.JwtUtils;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    @CrossOrigin
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            userService.register(dto.setRole("USER").setBalance(BigDecimal.ZERO));
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Регистрация прошла успешно"));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @CrossOrigin
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto, HttpServletResponse response) {
        try {
            User user = userService.authenticate(dto);
            String token = jwtUtils.generateToken(user.getUsername());
            Cookie cookie = new Cookie("AUTH_TOKEN", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(86400000);
            response.addCookie(cookie);

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok().body(user);
    }
}
