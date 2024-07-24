package com.fpt.cursus.repository;

import com.fpt.cursus.entity.OrdersDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersDetailRepo extends JpaRepository<OrdersDetail, Long> {

    List<OrdersDetail> findAllByOrdersId(Long id);

    List<OrdersDetail> findAllByIdIn(List<Long> ids);
}
