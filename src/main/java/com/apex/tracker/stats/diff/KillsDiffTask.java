package com.apex.tracker.stats.diff;

import com.apex.tracker.stats.dto.DataDto;
import com.apex.tracker.stats.dto.StatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.apex.tracker.mapper.DataDtoToStateEntityMapper.KILLS;

@Slf4j
@Component
public class KillsDiffTask extends AbstractStatDiffChainTask {

    @Override
    public void process(DiffWorkspace wspace) {
        if (wspace.getStat().getKills().compareTo(getKills(wspace.getData())) != 0) {
            log.info("Kills changed: {}", wspace.getStat().getPlatformUserHandle());
            wspace.setShouldSave(true);
        }

        nextOrExit(wspace);
    }

    private Long getKills(DataDto dataDto) {
        return dataDto.getData().getStats().stream()
                .filter(s -> KILLS.equals(s.getMetadata().getKey()))
                .findFirst()
                .map(StatsDto::getValue)
                .orElse(0L);
    }
}
