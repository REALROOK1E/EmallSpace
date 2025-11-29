package com.emallspace.marketing.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;

    @Column(name = "product_id", unique = true)
    private Long productId;

    @Column(name = "stock_count")
    private Integer stockCount;

    @Version // For Optimistic Locking
    private Integer version;
}
