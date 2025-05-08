package kz.findmyname284.springbootproject.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kz.findmyname284.springbootproject.dto.CheckoutDTO;
import kz.findmyname284.springbootproject.dto.ProductItemDTO;
import kz.findmyname284.springbootproject.enums.OrderStatus;
import kz.findmyname284.springbootproject.exception.InsufficientStockException;
import kz.findmyname284.springbootproject.model.CatalogProduct;
import kz.findmyname284.springbootproject.model.Order;
import kz.findmyname284.springbootproject.model.OrderItem;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.model.WarehouseProduct;
import kz.findmyname284.springbootproject.repository.CatalogProductRepository;
import kz.findmyname284.springbootproject.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CatalogProductRepository cProductRepository;

    public List<Order> findByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional
    public Long processOrder(CheckoutDTO dto, User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryAddress(user.getAddress());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (ProductItemDTO item : dto.products()) {
            Optional<CatalogProduct> optionalProduct = cProductRepository.findById(item.id());
            CatalogProduct product = optionalProduct.isPresent() ? optionalProduct.get() : null;
            if (product == null) {
                throw new RuntimeException("Продукт не найден");
            }

            WarehouseProduct baseProduct = product.getBaseProduct();

            Long remainingQty = item.quantity();

            if (remainingQty <= 0) {
                throw new InsufficientStockException("Недостаточно запасов для продукта: " + baseProduct.getName());
            }
            
            remainingQty = Math.min(product.getQuantity(), remainingQty);

            BigDecimal price = product.getPrice().multiply(BigDecimal.ONE.subtract(product.getDiscount().divide(BigDecimal.valueOf(100))));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPrice(price);
            orderItem.setQuantity(remainingQty);
            orderItem.setWarehouse(baseProduct.getWarehouse());
            orderItems.add(orderItem);

            product.setQuantity(product.getQuantity() - remainingQty);
            total = total.add(price);
            cProductRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);
        return savedOrder.getId();
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> findByStatusAndStatusUpdateTimeBefore(OrderStatus sending, LocalDateTime minusMinutes) {
        return orderRepository.findByStatusAndStatusUpdateTimeBefore(sending, minusMinutes);
    }

    public List<Order> findByStatusAndOrderDateBefore(OrderStatus sending, LocalDateTime minusMinutes) {
        return orderRepository.findByStatusAndOrderDateBefore(sending, minusMinutes);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
