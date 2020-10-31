package com.apex.tracker;

import com.apex.tracker.notification.telegram.TelegramPlayersNotificator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
@EnableScheduling
public class TrackerApplication implements CommandLineRunner {

    @Autowired
    private TelegramPlayersNotificator telegramPlayersNotificator;

    public static void main(String[] args) {
        SpringApplication.run(TrackerApplication.class, args);
    }

    public TrackerApplication() {
        ApiContextInitializer.init();
    }

    @Override
    public void run(String... args) throws Exception {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(telegramPlayersNotificator);
    }
}
