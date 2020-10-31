package com.apex.tracker.stats;

import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.mapper.DataDtoToStateEntityMapper;
import com.apex.tracker.notification.PlayersNotificator;
import com.apex.tracker.props.StatsProps;
import com.apex.tracker.repository.PlayerRepository;
import com.apex.tracker.repository.StatRepository;
import com.apex.tracker.stats.dto.DataDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsLoader {

    @Autowired
    private final StatsProps stateProps;
    private final HttpClient client = HttpClient.newHttpClient();

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private StatRepository statRepository;
    @Autowired
    private PlayersNotificator playersNotificator;

    @Scheduled(fixedDelay = 120_000)
    public void loadStats() {
        log.info("Load stats...");
        LocalDateTime updateTime = LocalDateTime.now();
        playerRepository.findAll().stream()
                .map(p -> loadStats(p.getName()))
                .filter(Objects::nonNull)
                .forEach(d -> processData(d, updateTime));
    }

    private DataDto loadStats(String name) {

        try {
            log.info("load stats for: {}", name);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(stateProps.getUrl(), name)))
                    .header("TRN-Api-Key", stateProps.getApiKey())
                    .build();

            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(response, DataDto.class);
        } catch (IOException | InterruptedException ex) {
            log.info("Failed to load, {}", name, ex);
        }

        return null;
    }

    private void processData(DataDto data, LocalDateTime updateTime) {
        statRepository.findLastByName(data.getData().getMetadata().getPlatformUserHandle())
                .ifPresentOrElse(stat -> this.checkIsChanged(stat, data, updateTime), () -> this.createNewRecord(data, updateTime));
    }

    private void checkIsChanged(StatEntity stat, DataDto data, LocalDateTime updateTime) {
        if (!stat.getLevel().equals(data.getData().getMetadata().getLevel())) {
            playersNotificator.levelUpMessage(DataDtoToStateEntityMapper.toEntity(data, updateTime));
            log.info("Level changed: {}", data.getData().getMetadata().getPlatformUserHandle());
            saveStat(data, updateTime);
        } else if (!stat.getRankName().equals(data.getData().getMetadata().getRankName())) {
            playersNotificator.rankUpMessage(DataDtoToStateEntityMapper.toEntity(data, updateTime));
            log.info("Ranked changed: {}", data.getData().getMetadata().getPlatformUserHandle());
            saveStat(data, updateTime);
        }
    }

    private void createNewRecord(DataDto data, LocalDateTime updateTime) {
        playersNotificator.rankUpMessage(DataDtoToStateEntityMapper.toEntity(data, updateTime));
        saveStat(data, updateTime);
    }

    private void saveStat(DataDto dataDto, LocalDateTime updateTime) {
        statRepository.save(DataDtoToStateEntityMapper.toEntity(dataDto, updateTime));
    }
}
