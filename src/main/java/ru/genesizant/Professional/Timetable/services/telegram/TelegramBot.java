package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.genesizant.Professional.Timetable.dto.BotProperties;

@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final MessageService messageService;

    @Autowired
    public TelegramBot(BotProperties botProperties, MessageService messageService) {
        super(botProperties.token());
        this.botProperties = botProperties;
        this.messageService = messageService;
    }

    @Override
    public String getBotUsername() {
        return botProperties.name();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            SendMessage sendMessage = messageService.messageReceiver(update);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
