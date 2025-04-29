package kz.findmyname284.springbootproject.model;

import java.io.Serializable;

public class OrderItemId implements Serializable {
    private Long order;
    private Long product;

    public OrderItemId() {
    }

    public OrderItemId(Long order, Long product) {
        this.order = order;
        this.product = product;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public Long getProduct() {
        return product;
    }

    public void setProduct(Long product) {
        this.product = product;
    }
}