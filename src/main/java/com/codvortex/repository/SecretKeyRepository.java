package com.codvortex.repository;

import com.codvortex.domain.SecretKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecretKeyRepository extends JpaRepository<SecretKeyEntity, String> {
}
