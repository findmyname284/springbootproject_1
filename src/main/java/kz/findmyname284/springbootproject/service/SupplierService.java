package kz.findmyname284.springbootproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.dto.SupplierDTO;
import kz.findmyname284.springbootproject.exception.AlreadyExistsException;
import kz.findmyname284.springbootproject.exception.ResourceNotFoundException;
import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public void register(SupplierDTO dto, User user) throws AlreadyExistsException {
        if (supplierRepository.existsByUser(user)) {
            throw new AlreadyExistsException("Пользователь уже поставщик");
        }
        
        if (supplierRepository.existsByEmail(dto.email())) {
            throw new AlreadyExistsException("Электронная почта уже существует");
        }

        if (supplierRepository.existsByPhone(dto.phone())) {
            throw new AlreadyExistsException("Телефон уже существует");
        }

        Supplier supplier = new Supplier();

        supplier.setName(dto.name());
        supplier.setAddress(dto.address());
        supplier.setPhone(dto.phone());
        supplier.setEmail(dto.email());
        supplier.setRegistrationDate(java.time.LocalDate.now());
        supplier.setUser(user);

        supplierRepository.save(supplier);
    }

    public Supplier findByUser(User user) {
        return supplierRepository.findByUser(user);
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier update(Long id, SupplierDTO supplierDto) throws ResourceNotFoundException {
        try {
            
            Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
            supplier.setName(supplierDto.name());
            supplier.setAddress(supplierDto.address());
            supplier.setPhone(supplierDto.phone());
            supplier.setEmail(supplierDto.email());
            return supplierRepository.save(supplier);
        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }

    public void deleteById(Long id) {
        supplierRepository.deleteById(id);
    }
}
