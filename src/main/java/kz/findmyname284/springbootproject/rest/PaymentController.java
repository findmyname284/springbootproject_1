package kz.findmyname284.springbootproject.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import kz.findmyname284.springbootproject.dto.PaymentDTO;
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
@EnableScheduling
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final UserService userService;
    private final OrderService orderService;
    private final CatalogProductRepository cProductRepository;

    @PostMapping
    public ResponseEntity<?> pay(@RequestBody PaymentDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authorization.getAuthorizationUser(userService, userDetails);
            System.out.println(dto);

            Order order = orderService.findById(dto.id());
            if (order == null) {
                throw new RuntimeException("Заказ не найден");
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new RuntimeException("Заказ не находится в обработке");
            }

            order.getItems().stream().forEach(item -> {
                User seller = item.getProduct().getSeller().getUser();
                seller.setBalance(
                        seller.getBalance().add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
                userService.save(seller);
            });

            order.setStatus(OrderStatus.SENDING);
            order.setStatusUpdateTime(LocalDateTime.now());
            orderService.save(order);
            return ResponseEntity.ok().body(Collections.singletonMap("id", order.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }

    }

    @PostMapping("/balance")
    public ResponseEntity<?> pay(@RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long id = body.get("id");
            if (id == null) {
                throw new IllegalArgumentException("требуется идентификатор");
            }
            User user = Authorization.getAuthorizationUser(userService, userDetails);
            Order order = orderService.findById(id);
            if (order == null) {
                throw new RuntimeException("Заказ не найден");
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new RuntimeException("Заказ не находится в обработке");
            }

            BigDecimal totalAmount = order.getItems().stream()
                    .map(item -> item.getProduct().getPrice()
                            .multiply(BigDecimal.ONE
                                    .subtract(item.getProduct().getDiscount().divide(BigDecimal.valueOf(100))))
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal balance = user.getBalance().subtract(totalAmount);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Недостаточный баланс");
            }

            user.setBalance(balance);
            userService.save(user);

            order.getItems().stream().forEach(item -> {
                User seller = item.getProduct().getSeller().getUser();
                seller.setBalance(
                        seller.getBalance().add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
                userService.save(seller);
            });

            order.setStatus(OrderStatus.SENDING);
            order.setStatusUpdateTime(LocalDateTime.now());
            orderService.save(order);
            return ResponseEntity.ok().body(Collections.singletonMap("id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }

    }

    @PostMapping("/balance/add")
    public ResponseEntity<?> addBalance(@RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = Authorization.getAuthorizationUser(userService, userDetails);
            int amount = Integer.parseInt(body.get("amount"));
            if (amount < 1000) {
                throw new RuntimeException("Минимальная сумма 1000");
            }

            user.setBalance(user.getBalance().add(BigDecimal.valueOf(amount)));
            BigDecimal balance = user.getBalance();

            user.setBalance(balance);
            userService.save(user);
            return ResponseEntity.ok().body(Collections.singletonMap("success", "Баланс успешно добавлен"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }

    }

    @Scheduled(fixedRate = 60_000 * 3)
    @Transactional
    public void checkDeliveries() {
        List<Order> orders = orderService
                .findByStatusAndStatusUpdateTimeBefore(
                        OrderStatus.SENDING,
                        LocalDateTime.now().minusMinutes(5));

        for (Order order : orders) {
            order.setStatus(OrderStatus.DELIVERED);
            order.setStatusUpdateTime(LocalDateTime.now());
            orderService.save(order);
        }
    }

    @Scheduled(fixedRate = 60_000 * 30)
    @Transactional
    public void checkPendings() {
        List<Order> orders = orderService
                .findByStatusAndOrderDateBefore(
                        OrderStatus.PENDING,
                        LocalDateTime.now().minusMinutes(30));

        System.out.println("Проверка отложенных ордеров...");

        for (Order order : orders) {
            System.out.println("Отмена ордера " + order.getId() + " " + order.getStatus());
            order.setStatus(OrderStatus.CANCELLED);
            order.setStatusUpdateTime(LocalDateTime.now());

            for (OrderItem item : order.getItems()) {
                CatalogProduct product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                cProductRepository.save(product);
            }

            orderService.save(order);
        }
    }
}
