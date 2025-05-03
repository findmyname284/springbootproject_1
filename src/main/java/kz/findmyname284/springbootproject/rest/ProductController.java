package kz.findmyname284.springbootproject.rest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kz.findmyname284.springbootproject.dto.WarehouseProductDTO;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AuthorizationException;
import kz.findmyname284.springbootproject.model.CatalogProduct;
import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.model.Warehouse;
import kz.findmyname284.springbootproject.model.WarehouseProduct;
import kz.findmyname284.springbootproject.repository.CatalogProductRepository;
import kz.findmyname284.springbootproject.repository.WarehouseProductRepository;
import kz.findmyname284.springbootproject.service.EmployeeService;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.service.WarehouseService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final WarehouseProductRepository wProductRepository;
    private final CatalogProductRepository cProductRepository;
    private final EmployeeService employeeService;
    private final UserService userService;
    private final WarehouseService warehouseService;

    private final String UPLOAD_DIR = "./uploads/";

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody WarehouseProductDTO productDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Authorization.checkRole(user, UserRole.MANAGER, UserRole.ADMIN, UserRole.EMPLOYEE);

            Optional<Employee> employee = employeeService.findByUser(user);

            Warehouse warehouse = null;

            if (!employee.isPresent()) {
                Authorization.checkRole(user, UserRole.ADMIN, UserRole.MANAGER);
                warehouse = warehouseService.findById(productDto.warehouseId());
            } else {
                warehouse = warehouseService.findByEmployee(employee.get());

            }

            if (warehouse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "Warehouse not found"));
            }

            WarehouseProduct product = new WarehouseProduct();
            product.setName(productDto.name());
            product.setDescription(productDto.description());
            product.setSku(productDto.sku());
            product.setImage(productDto.image() == null ? "/img/no-image.png" : productDto.image());
            product.setQuantity(productDto.quantity());
            product.setWarehouse(warehouse);
            product.setCreated(LocalDateTime.now());

            wProductRepository.save(product);

            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Ошибка создания продукта: " + e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> handleFileUpload(
            @RequestParam("file") MultipartFile file) {

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + fileName;

            System.out.println(fileUrl);

            return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to upload file"));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<CatalogProduct>> getProducts() {
        return ResponseEntity.ok(cProductRepository.findAll());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getMethodName(@RequestParam(name = "id", required = true) Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Authorization.checkRole(user, UserRole.MANAGER, UserRole.ADMIN, UserRole.EMPLOYEE);

            WarehouseProduct product = wProductRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + id));

            return ResponseEntity.ok().body(product);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Not found"));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProduct(@RequestBody WarehouseProductDTO productDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Authorization.checkRole(user, UserRole.EMPLOYEE, UserRole.MANAGER, UserRole.ADMIN);

            Optional<WarehouseProduct> productOptional = wProductRepository.findById(productDto.id());

            if (productOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            WarehouseProduct savedProduct = productOptional.get();
            savedProduct.setName(productDto.name());
            savedProduct.setDescription(productDto.description());
            savedProduct.setSku(productDto.sku());
            savedProduct.setImage(productDto.image() == null ? "/img/no-image.png" : productDto.image());
            savedProduct.setQuantity(productDto.quantity());
            try {
                Authorization.checkRole(user, UserRole.MANAGER, UserRole.ADMIN);
                Warehouse warehouse = warehouseService.findById(productDto.warehouseId());
                if (warehouse == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Collections.singletonMap("error", "Warehouse not found"));
                }
                savedProduct.setBasePrice(productDto.basePrice());
                savedProduct.setWarehouse(warehouse);
            } catch (AuthorizationException e) {
                // ignore
            }

            wProductRepository.save(savedProduct);

            return ResponseEntity.ok().body(Collections.singletonMap("success", "Product updated"));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}