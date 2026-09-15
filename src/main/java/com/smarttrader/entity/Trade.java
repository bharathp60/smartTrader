package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
public class Trade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    TradingOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    Position position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    Instrument instrument;

    @Column(nullable = false, length = 40)
    String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    OrderSide side;

    @Column(nullable = false, precision = 24, scale = 4)
    BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 8)
    BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 8)
    BigDecimal fees = BigDecimal.ZERO;

    @Column(nullable = false)
    Instant executedAt;

    public TradingOrder getOrder() { return order; }
    public void setOrder(TradingOrder order) { this.order = order; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}
