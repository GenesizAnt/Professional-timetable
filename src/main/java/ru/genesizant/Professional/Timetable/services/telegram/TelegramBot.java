package ru.genesizant.Professional.Timetable.services.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final MainMessageService mainMessageService;
    private final NotifyMessageService notifyMessageService;

    @Autowired
    public TelegramBot(BotProperties botProperties, MainMessageService mainMessageService, NotifyMessageService notifyMessageService) {
        super(botProperties.token());
        this.botProperties = botProperties;
        this.mainMessageService = mainMessageService;
        this.notifyMessageService = notifyMessageService;
//        try {
//            this.execute(new SetMyCommands(listMenuCommand(), new BotCommandScopeDefault(),null));
//        } catch (TelegramApiException e) {
//            log.error("Ошибка установки меню бота, пункты меню такие: " + listMenuCommand());
//        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = mainMessageService.messageReceiver(update);
        try {
            if (sendMessage.getText().startsWith("Пароль изменен и зашифрован")) {
                deleteMsgWithPassword(Integer.parseInt(sendMessage.getText().replaceAll("\\D", "")), Long.valueOf(sendMessage.getChatId()));
                sendMessage.setText("Пароль изменен и зашифрован");
            }
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправка сообщения: " + sendMessage.getText() + ". Текст ошибки: " + e.getMessage());
        }
    }

    private void deleteMsgWithPassword(Integer messageId, Long chatId) {
        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка удаления сообщения в чате: " + deleteMessage.getChatId() + ". Текст ошибки: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 */15 * * * *") // это 15 минут - "* */15 * * * *"
    public void notifySendler() {
        List<SendMessage> messageList = notifyMessageService.messageReceiver();
        try {
            for (SendMessage sendMessage : messageList) {
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщений по расписанию: " + messageList + ". Текст ошибки: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.name();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    private List<BotCommand> listMenuCommand() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начало работы"));
        listOfCommands.add(new BotCommand("/changepassword", "Изменить пароль"));
        return listOfCommands;
    }
}
