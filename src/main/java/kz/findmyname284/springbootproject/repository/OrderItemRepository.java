package kz.findmyname284.springbootproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.OrderItem;
import kz.findmyname284.springbootproject.model.OrderItemId;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}