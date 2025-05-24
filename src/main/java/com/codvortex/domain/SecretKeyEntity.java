package com.codvortex.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecretKeyEntity {
    @Id
    private String id = "jwt-secret";

    private String secret;

    public SecretKeyEntity(String secret) {
        this.id = "jwt-secret"; // Always store under the same ID
        this.secret = secret;
    }

}
