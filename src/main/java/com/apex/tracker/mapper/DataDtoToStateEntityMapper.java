package com.apex.tracker.mapper;

import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.stats.dto.DataDto;

import java.time.LocalDateTime;

public class DataDtoToStateEntityMapper {

    public static StatEntity toEntity(DataDto dataDto) {
        return StatEntity.builder()
                .avatarUrl(dataDto.getData().getMetadata().getAvatarUrl())
                .created(LocalDateTime.now())
                .level(dataDto.getData().getMetadata().getLevel())
                .platformUserHandle(dataDto.getData().getMetadata().getPlatformUserHandle())
                .rankImage(dataDto.getData().getMetadata().getRankImage())
                .rankName(dataDto.getData().getMetadata().getRankName())
                .build();
    }
}
