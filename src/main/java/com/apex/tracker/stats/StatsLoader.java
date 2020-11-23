package com.apex.tracker.stats;

import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.mapper.DataDtoToStateEntityMapper;
import com.apex.tracker.notification.PlayersNotificator;
import com.apex.tracker.props.StatsProps;
import com.apex.tracker.repository.PlayerRepository;
import com.apex.tracker.repository.StatRepository;
import com.apex.tracker.stats.diff.DiffTaskChain;
import com.apex.tracker.stats.diff.DiffWorkspace;
import com.apex.tracker.stats.dto.DataDto;
import com.apex.tracker.stats.dto.MetadataDto;
import com.apex.tracker.stats.dto.PlayerStatsDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsLoader {


    private final StatsProps stateProps;
    private final HttpClient client = HttpClient.newHttpClient();

    private final PlayerRepository playerRepository;
    private final StatRepository statRepository;
    private final PlayersNotificator playersNotificator;
    private final DiffTaskChain diffTaskChain;

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

        String response = null;
        try {
            log.info("load stats for: {}", name);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(stateProps.getUrl(), name)))
                    .header("TRN-Api-Key", stateProps.getApiKey())
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(response, DataDto.class);
        } catch (IOException | InterruptedException ex) {
            log.info("Failed to load, {}, response: {}", name, response, ex);
        }

        return null;
    }

    private void processData(DataDto data, LocalDateTime updateTime) {
        log.info("Data to process: {}", data);
        String userName = Optional.ofNullable(data)
                .map(DataDto::getData)
                .map(PlayerStatsDto::getMetadata)
                .map(MetadataDto::getPlatformUserHandle)
                .orElse(null);

        if (userName == null) {
            log.info("Empty user name.");
            return;
        }

        statRepository.findLastByName(userName)
                .ifPresentOrElse(stat -> this.checkIsChanged(stat, data, updateTime), () -> this.createNewRecord(data, updateTime));
    }

    private void checkIsChanged(StatEntity stat, DataDto data, LocalDateTime updateTime) {
        diffTaskChain.process(DiffWorkspace.builder()
                .stat(stat)
                .data(data)
                .updateTime(updateTime)
                .build());
    }

    private void createNewRecord(DataDto data, LocalDateTime updateTime) {
        playersNotificator.rankUpMessage(DataDtoToStateEntityMapper.toEntity(data, updateTime));
        saveStat(data, updateTime);
    }

    private void saveStat(DataDto dataDto, LocalDateTime updateTime) {
        statRepository.save(DataDtoToStateEntityMapper.toEntity(dataDto, updateTime));
    }
}
