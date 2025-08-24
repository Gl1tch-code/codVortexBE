package com.codvortex.repository;

import com.codvortex.domain.Order;
import com.codvortex.utils.OrderShippinStatusEnum;
import com.codvortex.utils.OrderStatusEnum;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Query("SELECT o FROM Order o WHERE (o.user.id = :userId OR o.product.user.id = :userId) AND (o.isSellerPayed = false OR o.isProductOwnerPayed = false)")
    List<Order> findAllByUserId(Long userId);

    @Query("""
    SELECT COUNT(o) 
    FROM Order o 
    WHERE o.user.id = :userId 
      AND o.shippingStatus = :shippingStatus 
      AND o.date BETWEEN :startOfMonth AND :endOfMonth
    """)
    Integer countByUserIdAndShippingStatusAndOrderDateBetween(
            @Param("userId") Long userId,
            @Param("shippingStatus") OrderShippinStatusEnum shippingStatus,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.updatedAt BETWEEN :updatedAtAfter AND :updatedAtBefore AND LOWER(o.country.key) = LOWER(:countryKey)")
    List<Order> findAllByUserIdAndUpdatedAtBetweenAndCountryKey(
            @Param("userId") Long userId,
            @Param("updatedAtAfter") LocalDateTime updatedAtAfter,
            @Param("updatedAtBefore") LocalDateTime updatedAtBefore,
            @Param("countryKey") String countryKey
    );

    default Page<Order> findByKeyword(
            Long userId,
            String keyword,
            OrderStatusEnum status,
            OrderShippinStatusEnum shippingStatus,
            String countryKey,
            Pageable pageable
    ) {
        return findAll((root, query, cb) -> {
            Predicate userPredicate = cb.equal(root.get("user").get("id"), userId);

            Predicate statusPredicate = status != null
                    ? cb.equal(cb.lower(root.get("status")), status.toString().toLowerCase())
                    : cb.conjunction();

            Predicate shippingStatusPredicate = shippingStatus != null
                    ? cb.equal(cb.lower(root.get("shippingStatus")), shippingStatus.toString().toLowerCase())
                    : cb.conjunction();

            Predicate countryPredicate = countryKey != null && !countryKey.isBlank()
                    ? cb.equal(cb.lower(root.get("country").get("key")), countryKey.toLowerCase())
                    : cb.conjunction();

            Predicate keywordPredicate = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String[] words = keyword.toLowerCase().trim().split("\\s+");

                Predicate addressPredicate = Arrays.stream(words)
                        .map(word -> cb.like(cb.lower(root.get("address")), "%" + word + "%"))
                        .reduce(cb::and)
                        .orElse(cb.conjunction());

                Predicate idPredicate = cb.like(
                        cb.function("str", String.class, root.get("id")),
                        "%" + keyword + "%"
                );

                keywordPredicate = cb.or(addressPredicate, idPredicate);
            }
            query.orderBy(cb.desc(root.get("id")));

            return cb.and(userPredicate, statusPredicate, shippingStatusPredicate, countryPredicate, keywordPredicate);
        }, pageable);
    }

    default Page<Order> findAsEmployeeByKeyword(
            Long userId,
            String keyword,
            OrderStatusEnum status,
            OrderShippinStatusEnum shippingStatus,
            Long country,
            Boolean paymentStatus, // <-- new param (true = paid, false = not paid)
            LocalDateTime startDate, // <-- new param
            LocalDateTime endDate,   // <-- new param
            Pageable pageable
    ) {
        return findAll((root, query, cb) -> {
            Predicate userPredicate = userId != null
                    ? cb.equal(root.get("user").get("id"), userId)
                    : cb.conjunction();

            Predicate statusPredicate = status != null
                    ? cb.equal(root.get("status"), status)
                    : cb.conjunction();

            Predicate shippingStatusPredicate = shippingStatus != null
                    ? cb.equal(root.get("shippingStatus"), shippingStatus)
                    : cb.conjunction();

            Predicate countryPredicate = country != null
                    ? cb.equal(root.get("country").get("id"), country)
                    : cb.conjunction();

            // ✅ Payment status predicate (checking both seller & product owner payments)
            Predicate paymentPredicate = cb.conjunction();
            if (paymentStatus != null) {
                // Example: consider order paid if both seller and product owner are paid
                paymentPredicate = paymentStatus
                        ? cb.and(cb.isTrue(root.get("isSellerPayed")), cb.isTrue(root.get("isProductOwnerPayed")))
                        : cb.or(cb.isFalse(root.get("isSellerPayed")), cb.isFalse(root.get("isProductOwnerPayed")));
            }

            // ✅ Date range predicate
            Predicate datePredicate = cb.conjunction();
            if (startDate != null && endDate != null) {
                datePredicate = cb.between(root.get("date"), startDate, endDate);
            } else if (startDate != null) {
                datePredicate = cb.greaterThanOrEqualTo(root.get("date"), startDate);
            } else if (endDate != null) {
                datePredicate = cb.lessThanOrEqualTo(root.get("date"), endDate);
            }

            // ✅ Keyword search
            Predicate keywordPredicate = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String[] words = keyword.toLowerCase().trim().split("\\s+");

                Predicate addressPredicate = Arrays.stream(words)
                        .map(word -> cb.like(cb.lower(root.get("address")), "%" + word + "%"))
                        .reduce(cb::and)
                        .orElse(cb.conjunction());

                Predicate idPredicate = cb.like(
                        cb.function("str", String.class, root.get("id")),
                        "%" + keyword + "%"
                );

                Predicate namePredicate = cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
                Predicate phonePredicate = cb.like(cb.lower(root.get("phoneNumber")), "%" + keyword.toLowerCase() + "%");

                keywordPredicate = cb.or(addressPredicate, idPredicate, namePredicate, phonePredicate);
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(
                    userPredicate,
                    statusPredicate,
                    shippingStatusPredicate,
                    countryPredicate,
                    paymentPredicate,
                    datePredicate,
                    keywordPredicate
            );
        }, pageable);
    }



}
