package com.codvortex.repository;

import com.codvortex.domain.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE p.user.email = :userEmail AND p.country.id = :countryId")
    List<Product> findByUserEmail(String userEmail, Long countryId);

    @Query("SELECT p FROM Product p WHERE p.country.id = :countryId AND p.isAvailableStock = true AND p.user.id != :userId")
    List<Product> findByCountryAndIsAvailableStock(Long countryId, Long userId);


    @Query("SELECT p FROM Product p JOIN p.sourcingProducts sp WHERE sp.sourcing.id = :sourcingId ")
    List<Product> findAllBySourcingId(Long sourcingId);

    default Page<Product> findByKeyword(
            Long userId,
            String keyword,
            String countryKey,
            Boolean isAvailableStockOnly,
            Pageable pageable
    ) {
        return findAll((root, query, cb) -> {
            Predicate userPredicate = cb.equal(root.get("user").get("id"), userId);

            Predicate countryPredicate = countryKey != null && !countryKey.isBlank()
                    ? cb.equal(cb.lower(root.get("country").get("key")), countryKey.toLowerCase())
                    : cb.conjunction();

            Predicate keywordPredicate = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String[] words = keyword.toLowerCase().trim().split("\\s+");

                Predicate namePredicate = Arrays.stream(words)
                        .map(word -> cb.like(cb.lower(root.get("name")), "%" + word + "%"))
                        .reduce(cb::and)
                        .orElse(cb.conjunction());

                Predicate idPredicate = cb.like(
                        cb.function("str", String.class, root.get("id")),
                        "%" + keyword + "%"
                );

                keywordPredicate = cb.or(namePredicate, idPredicate);
            }
            query.orderBy(cb.desc(root.get("id")));

            if (isAvailableStockOnly) {
                Predicate isAvailableStockOnlyPredicate = cb.equal(root.get("isAvailableStock"), isAvailableStockOnly);
                return cb.and(countryPredicate, keywordPredicate, isAvailableStockOnlyPredicate);
            }

            return cb.and(userPredicate, countryPredicate, keywordPredicate);
        }, pageable);
    }

}
