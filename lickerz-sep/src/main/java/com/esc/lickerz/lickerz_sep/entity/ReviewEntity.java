package com.esc.lickerz.lickerz_sep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "reviews")
public class ReviewEntity { 


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    private InstrumentEntity instrument;

    @Column(nullable = false)
    private double rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "likes", columnDefinition = "INTEGER DEFAULT 0")
    private int likes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewLikeEntity> likedUsers = new HashSet<>();

    // 기본 생성자
    public ReviewEntity() {}

    // 생성자
    public ReviewEntity(UserEntity user, InstrumentEntity instrument, double rating, String comment) {
        this.user = user;
        this.instrument = instrument;
        this.rating = rating;
        this.comment = comment;
    }

}
