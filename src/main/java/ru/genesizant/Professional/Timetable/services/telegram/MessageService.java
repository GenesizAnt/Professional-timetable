package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MessageService {

    public SendMessage messageReceiver(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();

            String response;
            switch (text) {
                case "/start" -> response = String.format("Привет %s! Я тг бот", name);
                case "/stop" -> response = String.format("До свидания %s!", name);
                default -> response = "Не понимать тебя";
            }

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(response);
            return sendMessage;
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Неизвестный запрос");
        return sendMessage;
    }
}
