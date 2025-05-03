package kz.findmyname284.springbootproject.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.Warehouse;
import kz.findmyname284.springbootproject.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public Warehouse findById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
    }

    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    public Warehouse save(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public void deleteById(Long warehouseId) {
        warehouseRepository.deleteById(warehouseId);
    }

    public Warehouse findByEmployee(Employee employee) {
        return warehouseRepository.findByManager(employee);
    }
}
