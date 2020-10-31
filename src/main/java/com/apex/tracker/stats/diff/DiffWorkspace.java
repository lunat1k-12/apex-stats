package com.apex.tracker.stats.diff;

import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.stats.dto.DataDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DiffWorkspace {

    private StatEntity stat;
    private DataDto data;
    private LocalDateTime updateTime;
    private boolean shouldSave;
}
