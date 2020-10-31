package com.apex.tracker.stats.dto;

import lombok.Data;

@Data
public class StatsDto {

    private StatsMetadataDto metadata;
    private Long value;
}
