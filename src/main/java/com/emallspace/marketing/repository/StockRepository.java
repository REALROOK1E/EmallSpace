package com.emallspace.marketing.repository;

import com.emallspace.marketing.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Stock s SET s.stockCount = s.stockCount - 1 WHERE s.productId = ?1 AND s.stockCount > 0")
    int decreaseStock(Long productId);
    
    // Note: @Version field handles the optimistic locking automatically with JPA save(), 
    // but for explicit high-concurrency SQL as described:
    // "UPDATE stock SET stock_count = stock_count - 1, version = version + 1 WHERE product_id = ? AND stock_count > 0 AND version = ?"
    // JPA's built-in optimistic locking is usually sufficient, but let's stick to the custom query style if needed.
}
