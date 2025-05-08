package kz.findmyname284.springbootproject.service;

import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.dto.SupplierDTO;
import kz.findmyname284.springbootproject.exception.AlreadyExistsException;
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
}
