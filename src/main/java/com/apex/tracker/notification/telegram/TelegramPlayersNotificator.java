package com.apex.tracker.notification.telegram;

import com.apex.tracker.entity.ChatEntity;
import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.notification.PlayersNotificator;
import com.apex.tracker.repository.ChatRepository;
import com.apex.tracker.repository.PlayerRepository;
import com.apex.tracker.repository.StatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramPlayersNotificator extends TelegramLongPollingBot implements PlayersNotificator {

    public static final String BOT_USERNAME = "Apex_stats_bot";

    public static final String BOT_TOKEN = "1369218836:AAED5-ONso8BAkp0PFdjChLscRinWS4IQ0c";

    private final ChatRepository chatRepository;

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void onUpdateReceived(Update update) {

        log.info("update recieved: {}", update);
        Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getNewChatMembers)
                .stream()
                .flatMap(List::stream)
                .filter(m -> BOT_USERNAME.equals(m.getUserName()))
                .findAny()
                .ifPresent(m -> saveChat(update));

        Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getLeftChatMember)
                .filter(m -> BOT_USERNAME.equals(m.getUserName()))
                .ifPresent(m -> deleteChat(update));
    }

    private void saveChat(Update update) {
          chatRepository.save(ChatEntity.builder()
                  .name(update.getMessage().getChat().getTitle())
                  .telegramId(update.getMessage().getChatId())
                  .build());
          log.info("Chat id:{}, saved", update.getMessage().getChatId());
    }

    private void deleteChat(Update update) {
        chatRepository.deleteByTelegramId(update.getMessage().getChatId());
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void levelUpMessage(StatEntity stat) {
        chatRepository.findAll().forEach(chat -> levelUpMessage(stat, chat));
    }

    private void levelUpMessage(StatEntity stat, ChatEntity chat) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chat.getTelegramId());
        photo.setCaption(String.format("%s повысил свой уровень до %s, поздравляем!!!",
                stat.getPlatformUserHandle(),
                stat.getLevel()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stat.getAvatarUrl()))
                .build();

        try {
            photo.setPhoto("level", client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body());
            execute(photo);

        } catch (IOException | InterruptedException | TelegramApiException e) {
            log.info("Failed to load image", e);
        }
    }

    @Override
    public void rankUpMessage(StatEntity stat) {
        chatRepository.findAll().forEach(chat -> rankUpMessage(stat, chat));
    }

    private void rankUpMessage(StatEntity stat, ChatEntity chat) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chat.getTelegramId());
        photo.setCaption(String.format("%s теперь выступает на уровне %s, поздравляем!!!",
                stat.getPlatformUserHandle(),
                stat.getRankName()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stat.getRankImage()))
                .build();

        try {
            photo.setPhoto("rank_level", client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body());
            execute(photo);

        } catch (IOException | InterruptedException | TelegramApiException e) {
            log.info("Failed to load image", e);
        }
    }
}