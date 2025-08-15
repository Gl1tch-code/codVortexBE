package com.codvortex.repository;

import com.codvortex.domain.Invoice;
import com.codvortex.utils.InvoiceStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvoiceRepository extends JpaRepository<Invoice, String>, JpaSpecificationExecutor<Invoice> {

    default Page<Invoice> findByKeyword(
            String q,
            Long userId,
            InvoiceStatus status,
            Pageable pageable
    ) {
        return findAll((root, query, cb) -> {
            Predicate userPredicate = cb.equal(root.get("user").get("id"), userId);

            Predicate statusPredicate = status != null
                    ? cb.equal(cb.lower(root.get("status")), status.toString().toLowerCase())
                    : cb.conjunction();

            Predicate idPredicate = cb.like(
                    cb.function("str", String.class, root.get("id")),
                    "%" + q + "%"
            );

            Predicate keywordPredicate = cb.conjunction();

            query.orderBy(cb.desc(root.get("updatedAt")));

            return cb.and(userPredicate, idPredicate, statusPredicate, keywordPredicate);
        }, pageable);
    }

}
