package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepo extends JpaRepository<Orders, Long> {
    Orders findOrdersById(Long id);
}
