package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.enums.status.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;

@DataJpaTest
class OrdersRepoTest {

    @Autowired
    private OrdersRepo ordersRepo;

    @Autowired
    private TestEntityManager entityManager;

    private Long ordersId;

    @BeforeEach
    void setUp() {
        Orders orders = new Orders();
        orders.setPrice(2d);
        orders.setStatus(OrderStatus.PAID);
        orders.setCreatedBy("admin");
        orders.setCreatedDate(new Date());
        orders.setOrderCourseJson("list course");
        entityManager.persist(orders);
        ordersId = orders.getId();
    }

    @Test
    void findOrdersByIdTest() {
        Orders orders = ordersRepo.findOrdersById(ordersId);
        Assertions.assertNotNull(orders);
    }
}
