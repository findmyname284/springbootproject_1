package kz.findmyname284.springbootproject.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.enums.OrderStatus;
import kz.findmyname284.springbootproject.model.Order;
import kz.findmyname284.springbootproject.model.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatusAndStatusUpdateTimeBefore(OrderStatus sending, LocalDateTime minusMinutes);

    List<Order> findByStatusAndOrderDateBefore(OrderStatus sending, LocalDateTime orderDate);
}
