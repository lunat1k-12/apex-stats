package com.apex.tracker.stats.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlayerStatsDto {

    private MetadataDto metadata;
    private List<StatsDto> stats;
}
