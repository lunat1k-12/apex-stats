package com.apex.tracker.mapper;

import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.stats.dto.DataDto;
import com.apex.tracker.stats.dto.StatsDto;

import java.time.LocalDateTime;

public class DataDtoToStateEntityMapper {

    private DataDtoToStateEntityMapper() {}

    public static final String RANK_SCORE = "RankScore";

    public static StatEntity toEntity(DataDto dataDto, LocalDateTime updateTime) {
        return StatEntity.builder()
                .avatarUrl(dataDto.getData().getMetadata().getAvatarUrl())
                .created(updateTime)
                .level(dataDto.getData().getMetadata().getLevel())
                .platformUserHandle(dataDto.getData().getMetadata().getPlatformUserHandle())
                .rankImage(dataDto.getData().getMetadata().getRankImage())
                .rankName(dataDto.getData().getMetadata().getRankName())
                .rankScore(dataDto.getData().getStats().stream()
                        .filter(s -> RANK_SCORE.equals(s.getMetadata().getKey()))
                        .findFirst()
                        .map(StatsDto::getValue)
                        .orElse(null))
                .build();
    }
}
