package kz.findmyname284.springbootproject.rest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kz.findmyname284.springbootproject.dto.ProductDTO;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.model.Product;
import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.service.ProductService;
import kz.findmyname284.springbootproject.service.SupplierService;
import kz.findmyname284.springbootproject.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final SupplierService supplierService;
    private final UserService userService;

    private final String UPLOAD_DIR = "./uploads/";

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
            }
            Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());

            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
            }

            User user = userOptional.get();

            if (user.getRole() != UserRole.SUPPLIER) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Supplier not found"));
            }

            Supplier supplier = supplierService.findByUser(user);

            if (supplier == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Supplier not found"));
            }

            Product product = new Product();
            product.setName(productDto.name());
            product.setDescription(productDto.description());
            product.setPrice(productDto.price());
            product.setBarcode(productDto.barcode());
            product.setCategory(productDto.category());
            product.setImage(productDto.image() == null ? "" : productDto.image());
            product.setCreated(java.time.LocalDateTime.now());
            product.setSupplier(supplier);

            productService.newProduct(product);

            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Ошибка создания продукта: " + e.getMessage()));
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
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/get")
    public ResponseEntity<Product> getMethodName(@RequestParam(name = "id", required = false) Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }
    
}