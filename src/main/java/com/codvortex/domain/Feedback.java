package com.codvortex.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;

    @Column(length = 2000)
    private String feedback;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;


}
