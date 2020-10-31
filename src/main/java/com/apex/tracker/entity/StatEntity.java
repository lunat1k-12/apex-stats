package com.apex.tracker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "stats")
@Builder
public class StatEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(name = "platform_user_handle")
    private String platformUserHandle;
    @Column(name = "level")
    private Long level;
    @Column(name = "rank_name")
    private String rankName;
    @Column(name = "rank_image")
    private String rankImage;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "created")
    private LocalDateTime created;
    @Column(name = "rank_score")
    private Long rankScore;
}
