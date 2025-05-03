package kz.findmyname284.springbootproject.service;

import java.math.BigDecimal;
import java.util.List;

import javax.naming.AuthenticationException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.dto.LoginDTO;
import kz.findmyname284.springbootproject.dto.RegisterDTO;
import kz.findmyname284.springbootproject.dto.UpdateUserRequest;
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

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void register(RegisterDTO dto) throws AlreadyExistsException {
        if (userRepo.existsByUsername(dto.username())) {
            throw new AlreadyExistsException("Username already exists");
        }

        if (userRepo.existsByEmail(dto.email())) {
            throw new AlreadyExistsException("Email already exists");
        }

        if (userRepo.existsByPhone(dto.phone())) {
            throw new AlreadyExistsException("Phone already exists");
        }

        User user = new User();

        user.setName(dto.name());
        user.setSurname(dto.surname());
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setAddress(dto.address());
        user.setPhone(dto.phone());
        user.setBalance(BigDecimal.ZERO);
        user.setRole(UserRole.USER);

        userRepo.save(user);
    }

    public User authenticate(LoginDTO dto) throws AuthenticationException {
        User user = userRepo.findByUsername(dto.username());
        if (user == null) {
            throw new AuthenticationException("Invalid username or password");
        }
        return user;
    }

    public void updateRole(User user, UserRole supplier) {
        user.setRole(supplier);
        userRepo.save(user);
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public User getById(Long id) {
        return userRepo.findById(id).get();
    }

    public Object updateUser(Long id, UpdateUserRequest request) {
        User user = userRepo.findById(id).get();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setBalance(request.balance());
        user.setRole(UserRole.valueOf(request.role()));
        return userRepo.save(user);
    }

    public List<User> findByRole(UserRole employee) {
        return userRepo.findByRole(employee);
    }

    public void save(User user) {
        userRepo.save(user);
    }
}