package kz.findmyname284.springbootproject.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.User;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUser(User user);

    Optional<Employee> findByIin(String iin);

}
