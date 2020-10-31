package com.apex.tracker.stats.diff;

import com.apex.tracker.mapper.DataDtoToStateEntityMapper;
import com.apex.tracker.notification.PlayersNotificator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LevelDiffTask extends AbstractStatDiffChainTask {

    private final PlayersNotificator playersNotificator;

    @Override
    public void process(DiffWorkspace wspace) {
        if (!wspace.getStat().getLevel().equals(wspace.getData().getData().getMetadata().getLevel())) {
            playersNotificator.levelUpMessage(DataDtoToStateEntityMapper.toEntity(wspace.getData(), wspace.getUpdateTime()));
            log.info("Level changed: {}", wspace.getData().getData().getMetadata().getPlatformUserHandle());
            wspace.setShouldSave(true);
        }
        nextOrExit(wspace);
    }
}
