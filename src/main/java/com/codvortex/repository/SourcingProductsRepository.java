package com.codvortex.repository;

import com.codvortex.domain.SourcingProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SourcingProductsRepository  extends JpaRepository<SourcingProducts, Long>, JpaSpecificationExecutor<SourcingProducts> {
}
