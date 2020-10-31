package com.apex.tracker.stats;

import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.mapper.DataDtoToStateEntityMapper;
import com.apex.tracker.notification.PlayersNotificator;
import com.apex.tracker.repository.PlayerRepository;
import com.apex.tracker.repository.StatRepository;
import com.apex.tracker.stats.dto.DataDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@Slf4j
@Component
public class StatsLoader {

    public static final String API_KEY = "9cfdbd65-8873-4683-8a58-5bda33b2e27d";
    private final HttpClient client;

    public StatsLoader() {
        client = HttpClient.newHttpClient();
    }

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private StatRepository statRepository;
    @Autowired
    private PlayersNotificator playersNotificator;

    @Scheduled(fixedDelay = 120_000)
    public void loadStats() {
        log.info("Load stats...");
        playerRepository.findAll().stream()
                .map(p -> loadStats(p.getName()))
                .filter(Objects::nonNull)
                .forEach(this::processData);
    }

    private DataDto loadStats(String name) {

        try {
            log.info("load stats for: {}", name);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://public-api.tracker.gg/apex/v1/standard/profile/2/%s", name)))
                    .header("TRN-Api-Key", API_KEY)
                    .build();

            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(response, DataDto.class);
        } catch (IOException | InterruptedException ex) {
            log.info("Failed to load, {}", name, ex);
        }

        return null;
    }

    private void processData(DataDto data) {
        statRepository.findLastByName(data.getData().getMetadata().getPlatformUserHandle())
                .ifPresentOrElse(stat -> this.checkIsChanged(stat, data), () -> this.createNewRecord(data));
    }

    private void checkIsChanged(StatEntity stat, DataDto data) {
        if (!stat.getLevel().equals(data.getData().getMetadata().getLevel())) {
            playersNotificator.levelUpMessage(DataDtoToStateEntityMapper.toEntity(data));
            saveStat(data);
        } else if (!stat.getRankName().equals(data.getData().getMetadata().getRankName())) {
            playersNotificator.rankUpMessage(DataDtoToStateEntityMapper.toEntity(data));
            saveStat(data);
        }
    }

    private void createNewRecord(DataDto data) {
        playersNotificator.rankUpMessage(DataDtoToStateEntityMapper.toEntity(data));
        saveStat(data);
    }

    private void saveStat(DataDto dataDto) {
        statRepository.save(DataDtoToStateEntityMapper.toEntity(dataDto));
    }
}
