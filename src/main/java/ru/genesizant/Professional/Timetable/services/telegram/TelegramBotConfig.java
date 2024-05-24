package ru.genesizant.Professional.Timetable.services.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@EnableScheduling
public class TelegramBotConfig {

    private final TelegramBot telegramBot;

    @Autowired
    public TelegramBotConfig(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            log.error("Ошибка создания ТГ бота. Текст ошибки: " + e.getMessage());
        }
    }
}
