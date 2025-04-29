package kz.findmyname284.springbootproject.service;

import java.math.BigDecimal;
import java.util.Optional;

import javax.naming.AuthenticationException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.dto.LoginDTO;
import kz.findmyname284.springbootproject.dto.RegisterDTO;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AlreadyExistsException;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void register(RegisterDTO dto) throws AlreadyExistsException {
        if (userRepo.existsByUsername(dto.username())) {
            throw new AlreadyExistsException("Username already exists");
        }

        if (userRepo.existsByEmail(dto.email())) {
            throw new AlreadyExistsException("Email already exists");
        }

        User user = new User();

        user.setName(dto.name());
        user.setSurname(dto.surname());
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setAddress(dto.address());
        user.setMoney(BigDecimal.ZERO);
        user.setRole(UserRole.USER);

        userRepo.save(user);
    }

    public User authenticate(LoginDTO dto) throws AuthenticationException {
        return userRepo.findByUsername(dto.username())
                .filter(user -> passwordEncoder.matches(dto.password(), user.getPassword()))
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
    }

    public void updateRole(User user, UserRole supplier) {
        user.setRole(supplier);
        userRepo.save(user);
    }
}