package kz.findmyname284.springbootproject.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.dto.CheckoutDTO;
import kz.findmyname284.springbootproject.model.Order;
import kz.findmyname284.springbootproject.model.OrderItem;
import kz.findmyname284.springbootproject.model.Product;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    public List<Order> findByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Long processOrder(CheckoutDTO dto, User user) {
        try {

            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setDeliveryAddress(user.getAddress());

            List<OrderItem> items = dto.products().stream()
                    .map(item -> {
                        Product product = productService.findById(item.id());
                        int stock = product.getStock() - item.quantity();
                        if (stock < 0) {
                            throw new RuntimeException("Not enough stock");
                        }
                        product.setStock(stock);
                        productService.save(product);

                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setProduct(product);
                        orderItem.setQuantity(item.quantity());
                        return orderItem;
                    })
                    .toList();

            order.setItems(items);

            orderRepository.save(order);

            return order.getId();
        } catch (Exception e) {
            return -1L;
        }
    }

    public void save(Order order) {
        orderRepository.save(order);
    }
}
