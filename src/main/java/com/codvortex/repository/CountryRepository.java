package com.codvortex.repository;

import com.codvortex.domain.Country;
import com.codvortex.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long>, JpaSpecificationExecutor<Country> {

    Optional<Country> findByKeyIgnoreCase(String key);

}
