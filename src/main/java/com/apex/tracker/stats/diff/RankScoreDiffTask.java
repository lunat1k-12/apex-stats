package com.apex.tracker.stats.diff;

import com.apex.tracker.stats.dto.DataDto;
import com.apex.tracker.stats.dto.StatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.apex.tracker.mapper.DataDtoToStateEntityMapper.RANK_SCORE;

@Slf4j
@Component
public class RankScoreDiffTask extends AbstractStatDiffChainTask {

    @Override
    public void process(DiffWorkspace wspace) {

        if (wspace.getStat().getRankScore().compareTo(getRankScore(wspace.getData())) != 0) {
            log.info("Rank score changed: {}", wspace.getStat().getPlatformUserHandle());
            wspace.setShouldSave(true);
        }

        nextOrExit(wspace);
    }

    private Long getRankScore(DataDto dataDto) {
        return dataDto.getData().getStats().stream()
                .filter(s -> RANK_SCORE.equals(s.getMetadata().getKey()))
                .findFirst()
                .map(StatsDto::getValue)
                .orElse(null);
    }
}
