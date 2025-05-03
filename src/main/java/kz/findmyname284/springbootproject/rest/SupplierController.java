package kz.findmyname284.springbootproject.rest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.findmyname284.springbootproject.dto.SupplierDTO;
import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.exception.AlreadyExistsException;
import kz.findmyname284.springbootproject.exception.InsufficientStockException;
import kz.findmyname284.springbootproject.model.CatalogProduct;
import kz.findmyname284.springbootproject.model.Sale;
import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.model.WarehouseProduct;
import kz.findmyname284.springbootproject.repository.CatalogProductRepository;
import kz.findmyname284.springbootproject.repository.SaleRepository;
import kz.findmyname284.springbootproject.repository.WarehouseProductRepository;
import kz.findmyname284.springbootproject.service.SupplierService;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {
    private final UserService userService;
    private final SupplierService supplierService;
    private final WarehouseProductRepository wProductRepository;
    private final CatalogProductRepository cProductRepository;
    private final SaleRepository saleRepository;

    @PostMapping("/register")
    @CrossOrigin
    public ResponseEntity<?> register(@Valid @RequestBody SupplierDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
        }
        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Unauthorized"));
        }
        try {
            supplierService.register(dto, user);
            UserRole[] roles = { UserRole.MANAGER, UserRole.ADMIN };
            Map<String, String> response = Collections.singletonMap("success", "You successfully registered");
            for (UserRole role : roles) {
                if (user.getRole() == role) {
                    return ResponseEntity.ok().body(response);
                }
            }
            userService.updateRole(user, UserRole.SUPPLIER);
            return ResponseEntity.ok().body(response);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/buy/{id}")
    public ResponseEntity<?> buy(@PathVariable Long id, @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Authorization.checkRole(user, UserRole.SUPPLIER, UserRole.MANAGER, UserRole.ADMIN);

            Long quantity = body.get("value");
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid quantity"));
            }

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid product id"));
            }

            WarehouseProduct wp = wProductRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + id));

            if (wp.getQuantity() < quantity) {
                throw new InsufficientStockException();
            }

            wp.setQuantity(wp.getQuantity() - quantity);

            if (user.getBalance().compareTo(wp.getBasePrice().multiply(BigDecimal.valueOf(quantity))) < 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Insufficient balance"));
            }
            user.setBalance(user.getBalance().subtract(wp.getBasePrice().multiply(BigDecimal.valueOf(quantity))));

            Sale sale = new Sale();
            sale.setProduct(wp);
            sale.setQuantity(quantity);
            sale.setTimestamp(java.time.LocalDateTime.now());
            sale.setUser(user);
            sale.setWarehouse(wp.getWarehouse());
            saleRepository.save(sale);

            Supplier supplier = supplierService.findByUser(user);

            if (supplier == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Supplier not found"));
            }

            CatalogProduct cp = cProductRepository.findBySellerAndBaseProduct(supplier, wp);

            if (cp == null) {
                cp = new CatalogProduct();
                cp.setSeller(supplier);
                cp.setBaseProduct(wp);
                cp.setPrice(wp.getBasePrice().multiply(BigDecimal.valueOf(1.15)));
                cp.setDiscount(BigDecimal.ZERO);
                cp.setQuantity(quantity);
            } else {
                cp.setQuantity(cp.getQuantity() + quantity);
            }

            cProductRepository.save(cp);
            wProductRepository.save(wp);

            return ResponseEntity.ok().body(Collections.singletonMap("success", "Successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> edit(@PathVariable Long id, @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Authorization.checkRole(user, UserRole.SUPPLIER, UserRole.MANAGER, UserRole.ADMIN);

            BigDecimal price = BigDecimal.valueOf((int) body.get("price"));
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid price"));
            }

            BigDecimal discount = BigDecimal.valueOf((int) body.get("discount"));
            if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0
                    || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid discount"));
            }

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid product id"));
            }

            CatalogProduct cp = cProductRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + id));

            if (cp.getSeller().getId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "You can't edit this product"));
            }

            cp.setPrice(price);
            cp.setDiscount(discount);
            cProductRepository.save(cp);

            return ResponseEntity.ok().body(Collections.singletonMap("success", "Successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);
            Authorization.checkRole(user, UserRole.SUPPLIER, UserRole.MANAGER, UserRole.ADMIN);

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid product id"));
            }

            CatalogProduct cp = cProductRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + id));

            if (cp.getSeller().getId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "You can't view this product"));
            }

            return ResponseEntity.ok(cp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

}
