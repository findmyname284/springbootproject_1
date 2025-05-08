package kz.findmyname284.springbootproject.rest;

import java.math.BigDecimal;
import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.findmyname284.springbootproject.dto.CheckoutDTO;
import kz.findmyname284.springbootproject.enums.OrderStatus;
import kz.findmyname284.springbootproject.model.CatalogProduct;
import kz.findmyname284.springbootproject.model.Order;
import kz.findmyname284.springbootproject.model.OrderItem;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.repository.CatalogProductRepository;
import kz.findmyname284.springbootproject.service.OrderService;
import kz.findmyname284.springbootproject.service.UserService;
import kz.findmyname284.springbootproject.utils.Authorization;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CatalogProductRepository cProductRepository;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);

            Long id = orderService.processOrder(dto, user);

            if (id == -1L) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Обработка заказа не удалась"));
            }

            return ResponseEntity.ok()
                    .body(Collections.singletonMap("id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);
            Order order = orderService.findById(orderId);

            if (order == null || !order.getUser().equals(user)) {
                throw new RuntimeException("Заказ не найден");
            }

            if (order.getStatus() == OrderStatus.SENDING) {
                BigDecimal totalAmount = order.getItems().stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal balance = user.getBalance().add(totalAmount);
                user.setBalance(balance);
                userService.save(user);
                for (OrderItem item : order.getItems()) {
                    CatalogProduct product = item.getProduct();
                    User seller = product.getSeller().getUser();
                    seller.setBalance(seller.getBalance().subtract(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    userService.save(seller);
                    cProductRepository.save(product);
                }

                order.setStatus(OrderStatus.CANCELLED);
            } else if (order.getStatus() == OrderStatus.PENDING) {
                for (OrderItem item : order.getItems()) {
                    CatalogProduct product = item.getProduct();
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    cProductRepository.save(product);
                }
                order.setStatus(OrderStatus.CANCELLED);
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Невозможно отменить заказ"));
            }

            orderService.save(order);

            return ResponseEntity.ok().body(Collections.singletonMap("success", "Заказ отменен"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
