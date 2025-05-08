package kz.findmyname284.springbootproject.service;

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
            throw new AlreadyExistsException("Имя пользователя уже существует");
        }

        if (userRepo.existsByEmail(dto.email())) {
            throw new AlreadyExistsException("Электронная почта уже существует");
        }

        if (userRepo.existsByPhone(dto.phone())) {
            throw new AlreadyExistsException("Телефон уже существует");
        }

        User user = new User();

        user.setName(dto.name());
        user.setSurname(dto.surname());
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setAddress(dto.address());
        user.setPhone(dto.phone());
        user.setBalance(dto.balance());
        user.setRole(UserRole.valueOf(dto.role()));

        userRepo.save(user);
    }

    public User authenticate(LoginDTO dto) throws AuthenticationException {
        User user = userRepo.findByUsername(dto.username());
        if (user == null) {
            throw new AuthenticationException("Неверное имя пользователя или пароль");
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

    public User updateUser(Long id, UpdateUserRequest dto) throws AlreadyExistsException {
        User user = userRepo.findById(id).get();
        if (userRepo.existsByUsername(dto.username()) && !user.getUsername().equals(dto.username())) {
            throw new AlreadyExistsException("Имя пользователя уже существует");
        }

        if (userRepo.existsByEmail(dto.email()) && !user.getEmail().equals(dto.email())) {
            throw new AlreadyExistsException("Электронная почта уже существует");
        }

        if (userRepo.existsByPhone(dto.phone()) && !user.getPhone().equals(dto.phone())) {
            throw new AlreadyExistsException("Телефон уже существует");
        }

        user.setName(dto.name());
        user.setSurname(dto.surname());
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setAddress(dto.address());
        user.setPhone(dto.phone());
        user.setBalance(dto.balance());
        user.setRole(UserRole.valueOf(dto.role()));
        return userRepo.save(user);
    }

    public List<User> findByRole(UserRole employee) {
        return userRepo.findByRole(employee);
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public void deleteById(Long id) {
        userRepo.deleteById(id);
    }
}