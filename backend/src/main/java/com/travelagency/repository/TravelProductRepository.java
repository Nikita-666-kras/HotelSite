package com.travelagency.repository;

import com.travelagency.domain.ProductType;
import com.travelagency.domain.TravelProduct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TravelProductRepository extends JpaRepository<TravelProduct, Long> {

    List<TravelProduct> findAllByOrderByPriceAsc();

    List<TravelProduct> findAllByOrderByIdDesc();

    @Query(
            """
            SELECT p FROM TravelProduct p WHERE
            (:type IS NULL OR p.type = :type)
            AND (:originPattern IS NULL OR LOWER(p.origin) LIKE :originPattern)
            AND (:destinationPattern IS NULL OR LOWER(p.destination) LIKE :destinationPattern)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:departAfter IS NULL OR p.departAt IS NULL OR p.departAt >= :departAfter)
            AND (:checkInFrom IS NULL OR p.checkIn IS NULL OR p.checkIn >= :checkInFrom)
            ORDER BY p.price ASC
            """)
    List<TravelProduct> search(
            @Param("type") ProductType type,
            @Param("originPattern") String originPattern,
            @Param("destinationPattern") String destinationPattern,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("departAfter") LocalDateTime departAfter,
            @Param("checkInFrom") LocalDate checkInFrom);

    @Query(
            """
            SELECT DISTINCT p.origin FROM TravelProduct p
            WHERE LOWER(p.origin) LIKE :pattern
            ORDER BY p.origin
            """)
    List<String> findDistinctOriginsMatching(@Param("pattern") String pattern);

    @Query(
            """
            SELECT DISTINCT p.destination FROM TravelProduct p
            WHERE LOWER(p.destination) LIKE :pattern
            ORDER BY p.destination
            """)
    List<String> findDistinctProductDestinationsMatching(@Param("pattern") String pattern);
}
