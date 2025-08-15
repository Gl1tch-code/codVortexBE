package com.codvortex.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private long size;

    @Lob
    @Column(name = "data")
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

}