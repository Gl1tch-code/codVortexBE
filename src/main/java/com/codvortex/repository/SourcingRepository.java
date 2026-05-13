package com.codvortex.repository;

import com.codvortex.domain.Sourcing;
import com.codvortex.domain.User;
import com.codvortex.utils.SourcingStatusEnum;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.List;

public interface SourcingRepository extends JpaRepository<Sourcing, Long>, JpaSpecificationExecutor<Sourcing> {

    @Query("Select s FROM Sourcing s WHERE s.user.id = :userId AND s.isShippingFeesPayed = false")
    List<Sourcing> findAllByUserIdAndIsShippingFeesPayedFalse(Long userId);

    Sourcing findByIdAndUserId(Long id, Long userId);


    default Page<Sourcing> findByAdminKeyword(
            Long userId,
            String keyword,
            String countryKey,
            SourcingStatusEnum status,
            Pageable pageable
    ) {
        return findAll((root, query, cb) -> {
            Predicate userPredicate = userId != null ? cb.equal(root.get("user").get("id"), userId) : cb.conjunction();

            Predicate countryPredicate = countryKey != null && !countryKey.isBlank()
                    ? cb.equal(cb.lower(root.get("country").get("key")), countryKey.toLowerCase())
                    : cb.conjunction();

            Predicate statusPredicate = status != null ? cb.equal(root.get("status"), status) : cb.conjunction();

            Predicate keywordPredicate = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String[] words = keyword.toLowerCase().trim().split("\\s+");

                Predicate titlePredicate = Arrays.stream(words)
                        .map(word -> cb.like(cb.lower(root.get("title")), "%" + word + "%"))
                        .reduce(cb::and)
                        .orElse(cb.conjunction());

                Predicate idPredicate = cb.like(
                        cb.function("str", String.class, root.get("id")),
                        "%" + keyword + "%"
                );

                keywordPredicate = cb.or(titlePredicate, idPredicate);
            }
            query.orderBy(cb.desc(root.get("id")));

            return cb.and(userPredicate, countryPredicate, keywordPredicate, statusPredicate);
        }, pageable);
    }

    default Page<Sourcing> findByKeyword(
            Long userId,
            String keyword,
            String countryKey,
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

                Predicate titlePredicate = Arrays.stream(words)
                        .map(word -> cb.like(cb.lower(root.get("title")), "%" + word + "%"))
                        .reduce(cb::and)
                        .orElse(cb.conjunction());

                Predicate idPredicate = cb.like(
                        cb.function("str", String.class, root.get("id")),
                        "%" + keyword + "%"
                );

                keywordPredicate = cb.or(titlePredicate, idPredicate);
            }
            query.orderBy(cb.desc(root.get("id")));

            return cb.and(userPredicate, countryPredicate, keywordPredicate);
        }, pageable);
    }

}
