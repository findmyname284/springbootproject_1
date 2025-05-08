package kz.findmyname284.springbootproject.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

import jakarta.persistence.EntityNotFoundException;
import kz.findmyname284.springbootproject.dto.EmployeeDTO;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AuthorizationException;
import kz.findmyname284.springbootproject.exception.ResourceNotFoundException;
import kz.findmyname284.springbootproject.model.Category;
import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.SalaryHistory;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.model.Warehouse;
import kz.findmyname284.springbootproject.repository.CategoryRepository;
import kz.findmyname284.springbootproject.repository.SalaryHistoryRepository;
import kz.findmyname284.springbootproject.service.EmployeeService;
import kz.findmyname284.springbootproject.service.SalaryCalculationResult;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.service.WarehouseService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerRestController {
    private final UserService userService;
    private final WarehouseService warehouseService;
    private final EmployeeService employeeService;
    private final CategoryRepository categoryRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;

    @GetMapping("warehouses")
    public ResponseEntity<?> getWarehouses(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }

        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserRole[] roles = { UserRole.MANAGER, UserRole.ADMIN };
        for (UserRole role : roles) {
            if (user.getRole() == role) {
                return ResponseEntity.ok(warehouseService.findAll());
            }
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("warehouses")
    public ResponseEntity<?> addWarehouse(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }

        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserRole[] roles = { UserRole.MANAGER, UserRole.ADMIN };
        for (UserRole role : roles) {
            if (user.getRole() == role) {
                Warehouse warehouse = new Warehouse();
                if (body.get("name") == null || body.get("address") == null) {
                    return ResponseEntity.badRequest().build();
                }
                if (body.get("name").isEmpty() || body.get("address").isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                warehouse.setName(body.get("name"));
                warehouse.setAddress(body.get("address"));
                return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.save(warehouse));
            }
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("warehouses")
    public ResponseEntity<?> deleteWarehouse(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> body) {
        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }

        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserRole[] roles = { UserRole.MANAGER, UserRole.ADMIN };
        for (UserRole role : roles) {
            if (user.getRole() == role) {
                Long warehouseId = body.get("id");
                if (warehouseId == null) {
                    return ResponseEntity.badRequest().build();
                }
                warehouseService.deleteById(warehouseId);
                return ResponseEntity.ok().body(Collections.singletonMap("success", "Успешно удалено"));
            }
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("warehouses/{id}")
    public ResponseEntity<?> getWarehouseById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }

        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserRole[] roles = { UserRole.MANAGER, UserRole.ADMIN };
        for (UserRole role : roles) {
            if (user.getRole() == role) {
                Warehouse warehouse = warehouseService.findById(id);
                if (warehouse == null) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(warehouse);
            }
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("warehouses/{id}")
    public ResponseEntity<?> updateWarehouse(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);

            Warehouse warehouse = warehouseService.findById(id);
            if (warehouse == null) {
                return ResponseEntity.notFound().build();
            }
            if (body.get("name") != null && !body.get("name").isEmpty()) {
                warehouse.setName(body.get("name"));
            }
            if (body.get("address") != null && !body.get("address").isEmpty()) {
                warehouse.setAddress(body.get("address"));
            }
            if (body.get("employee-id") != null && !body.get("employee-id").isEmpty()) {
                Long employeeId = Long.parseLong(body.get("employee-id"));
                Employee employee = employeeService.findById(employeeId).isPresent()
                        ? employeeService.findById(employeeId).get()
                        : null;
                if (employee == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Collections.singletonMap("error", "Сотрудник не найден"));
                }

                Warehouse existingWarehouse = warehouseService.findByEmployee(employee);

                if (existingWarehouse != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonMap("error", "У сотрудника уже есть склад"));
                }

                warehouse.setManager(employee);
            }
            warehouseService.save(warehouse);
            return ResponseEntity.ok().body(Collections.singletonMap("success", "Успешно обновлено"));

        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("employees")
    public ResponseEntity<?> getAllEmployees(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            return ResponseEntity.ok(employeeService.findAll());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("employees")
    public ResponseEntity<?> createEmployee(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody EmployeeDTO request) {

        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);

            User user = userService.getById(request.userId());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Collections.singletonMap("error", "Пользователь не найден"));
            }

            if (employeeService.findByUser(user).isPresent()) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Пользователь уже является сотрудником"));
            }

            if (user.getRole() == UserRole.SUPPLIER) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Пользователь уже является поставщиком"));
            }

            if (request.iin().length() != 12) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Неверный ИИН"));
            }

            if (employeeService.findByIin(request.iin()).isPresent()) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Сотрудник с таким номером уже существует"));
            }

            if (user.getRole() == UserRole.USER) {
                user.setRole(UserRole.EMPLOYEE);
            }

            Employee employee = new Employee();
            employee.setUser(user);
            employee.setHireDate(LocalDate.now());
            employee.setSalary(request.salary());
            employee.setIin(request.iin());

            Employee savedEmployee = employeeService.save(employee);
            userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("employees/{id}")
    public ResponseEntity<?> getEmployeeById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Сотрудник не найден"));
            return ResponseEntity.ok(employee);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("employees/{id}")
    public ResponseEntity<?> updateEmployee(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody EmployeeDTO request) {

        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);

            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Сотрудник не найден"));

            SalaryHistory lastEntry = salaryHistoryRepository.findFirstByEmployeeOrderByStartDateDesc(employee);

            if (lastEntry != null) {
                lastEntry.setEndDate(LocalDate.now().minusDays(1));
                salaryHistoryRepository.save(lastEntry);
            }

            if (request.salary() == null) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Зарплата не может быть пустой"));
            }
            if (request.iin() == null) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "ИИН не может быть пустой"));
            }

            if (request.iin().length() != 12) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Неверный ИИН"));
            }

            if (employeeService.findByIin(request.iin()).isPresent() && !request.iin().equals(employee.getIin())) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Сотрудник с таким номером уже существует"));
            }

            SalaryHistory newEntry = new SalaryHistory();
            newEntry.setEmployee(employee);
            newEntry.setSalary(request.salary());
            newEntry.setStartDate(LocalDate.now());
            salaryHistoryRepository.save(newEntry);

            employee.setIin(request.iin());
            employee.setSalary(request.salary());

            Employee updatedEmployee = employeeService.save(employee);
            return ResponseEntity.ok(updatedEmployee);

        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("employees/{id}")
    public ResponseEntity<?> deleteEmployee(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            employeeService.deleteById(id);
            return ResponseEntity.ok().body(
                    Collections.singletonMap("success", "Сотрудник успешно удален"));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("employees/users")
    public ResponseEntity<?> getUsersWithEmployeeRole(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            return ResponseEntity.ok(userService.findByRole(UserRole.USER));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/employees/{id}/salary/{yearMonth}")
    public ResponseEntity<?> getSalaryDetails(
            @PathVariable Long id,
            @PathVariable String yearMonth) {

        try {
            YearMonth targetMonth = YearMonth.parse(yearMonth);
            SalaryCalculationResult result = employeeService.calculateMonthlySalary(id, targetMonth);

            if (result == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalSalary", result.getTotalSalary());
            response.put("daysPerSalary", result.getDaysPerSalary());
            response.put("dailyRate", result.getTotalSalary()
                    .divide(BigDecimal.valueOf(result.getDaysPerSalary()
                            .values().stream().mapToLong(Long::longValue).sum()), 2, RoundingMode.HALF_UP));

            return ResponseEntity.ok(response);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ArithmeticException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("categories")
    public ResponseEntity<?> getCategories(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            return ResponseEntity.ok(categoryRepository.findAll());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("categories/{id}")
    public ResponseEntity<?> getCategoryById(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            return ResponseEntity.ok(categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена")));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("categories")
    public ResponseEntity<?> addCategory(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> category) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            if (category.get("name") == null || category.get("name").isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Название категории не может быть пустым"));
            }
            Category newCategory = new Category();
            newCategory.setName(category.get("name"));
            categoryRepository.save(newCategory);
            return ResponseEntity.ok().body(
                    Collections.singletonMap("success", "Категория успешно добавлена"));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("categories/{id}")
    public ResponseEntity<?> updateCategory(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id, @RequestBody Map<String, String> category) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            Category existingCategory = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));
            if (category.get("name") == null || category.get("name").isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Название категории не может быть пустым"));
            }
            existingCategory.setName(category.get("name"));
            categoryRepository.save(existingCategory);
            return ResponseEntity.ok().body(
                    Collections.singletonMap("success", "Категория успешно обновлена"));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("categories/{id}")
    public ResponseEntity<?> deleteCategory(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            Authorization.checkAuthorization(userService, userDetails, UserRole.MANAGER, UserRole.ADMIN);
            categoryRepository.deleteById(id);
            return ResponseEntity.ok().body(
                    Collections.singletonMap("success", "Категория успешно удалена"));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
