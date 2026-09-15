package com.smarttrader.repository;

import com.smarttrader.entity.OrderStatus;
import com.smarttrader.entity.TradingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<TradingOrder, UUID> {
    Optional<TradingOrder> findByClientOrderId(String clientOrderId);
    Optional<TradingOrder> findByIdempotencyKey(String key);
    List<TradingOrder> findByStatusIn(Collection<OrderStatus> statuses);
    List<TradingOrder> findBySymbolAndStatusIn(String symbol, Collection<OrderStatus> statuses);
    boolean existsByIdempotencyKey(String key);
    long countByStatus(OrderStatus status);
}
