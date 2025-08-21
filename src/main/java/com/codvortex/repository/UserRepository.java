package com.codvortex.repository;

import com.codvortex.domain.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("""
                SELECT u FROM User u
                    WHERE LOWER(REPLACE(u.email, ' ', '')) = LOWER(REPLACE(:username, ' ', ''))
                        OR LOWER(REPLACE(u.fullName, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:username, ' ', ''), '%'))
                        OR u.phoneNumber = :username
            """)
    Optional<User> findByUsername(String username);

    @Query("""
                SELECT u FROM User u
                    WHERE u.role = 'ADMIN' AND LOWER(REPLACE(u.email, ' ', '')) = LOWER(REPLACE(:username, ' ', ''))
                        OR LOWER(REPLACE(u.fullName, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:username, ' ', ''), '%'))
                        OR u.phoneNumber = :username
            
            """)
    Optional<User> findByUsernameAndAdmin(String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);


    default Page<User> findByKeyword(
            String keyword,
            Pageable pageable
    ) {
        return findAll((root, query, cb) -> {
            Predicate keywordPredicate = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String[] words = keyword.toLowerCase().trim().split("\\s+");

                Predicate emailPredicate = Arrays.stream(words)
                        .map(word -> cb.like(cb.lower(root.get("email")), "%" + word + "%"))
                        .reduce(cb::and)
                        .orElse(cb.conjunction());

                Predicate phonePredicate = Arrays.stream(words)
                        .map(word -> cb.like(cb.lower(root.get("phoneNumber")), "%" + word + "%"))
                        .reduce(cb::and)
                        .orElse(cb.conjunction());

                Predicate idPredicate = cb.like(
                        cb.function("str", String.class, root.get("id")),
                        "%" + keyword + "%"
                );

                keywordPredicate = cb.or(emailPredicate, phonePredicate, idPredicate);
            }
            query.orderBy(cb.desc(root.get("id")));

            return cb.and(keywordPredicate);
        }, pageable);
    }


}
