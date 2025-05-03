package kz.findmyname284.springbootproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }

    public Optional<Employee> findByUser(User user) {
        return employeeRepository.findByUser(user);
    }

    public Optional<Employee> findByIin(String iin) {
        return employeeRepository.findByIin(iin);
    }
}

