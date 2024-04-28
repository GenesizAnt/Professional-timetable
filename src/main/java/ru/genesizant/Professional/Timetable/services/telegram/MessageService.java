package ru.genesizant.Professional.Timetable.services.telegram;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    //ToDo сайт с кодами смайлов тут https://emojipedia.org/smiling-face-with-smiling-eyes
    //ToDo вынести все сообщения и смайлы в отдельный класс констант или только смайлы?

    @Value("${email.regexp}")
    private String regex;

//    private final UserTelegramRepository userTelegramRepository;
    private final PersonService personService;
    private final UserTelegramService userTelegramService;

    @Autowired
    public MessageService(PersonService personService, UserTelegramService userTelegramService) {
        this.personService = personService;
        this.userTelegramService = userTelegramService;
    }

    //Получает и обрабатывает сообщение отправленное в бот
    public SendMessage messageReceiver(Update update) {
        ReplyKeyboardMarkup keyboardMarkup = getReplyKeyboardMarkup();
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();

            String response;
            switch (text) {
                case "/start" -> {
                    if (userTelegramService.isUserNotFoundById(chatId)) {
                        registerUser(chatId, update.getMessage().getChat());
                        response = EmojiParser.parseToUnicode(String.format("Привет %s!" +
                                " Для использования всех функций бота необходимо ввести email, который был" +
                                " использован в приложении \"Время профессионалов\" %s", name, ":blush:"));
                    } else if (userTelegramService.isUserEmailEmpty(chatId)) {
                        response = EmojiParser.parseToUnicode(String.format("Привет %s! Напоминаю, что для" +
                                " использования всех функций бота необходимо ввести email, который был использован" +
                                " в приложении \"Время профессионалов\" %s", name, ":blush:"));
                    } else {
//                        response = String.format("Привет %s! Чем могу помочь?", name);
                        response = EmojiParser.parseToUnicode(String.format("Привет %s! Чем могу помочь? %s", name, ":blush:"));
                    }
                }
                case "/stop" -> response = String.format("До свидания %s!", name);
                case "/button" -> {
                    response = String.format("Ня %s!", name);
                }
                default -> {
                    response = getResponse(text, chatId, name);
                }
            }
            return getReceiveMessage(chatId, response, keyboardMarkup);
        }
        return getReceiveMessage(update.getMessage().getChatId(), "Неизвестный запрос", keyboardMarkup);
    }

    //Добавляем поддержку экранной клавиатуры
    private ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("погода");
        row.add("шутка");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("регистрация");
        row.add("проверить почту");
        row.add("удалить почту");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    //Обработка разных сценариев неизвестного сообщения от пользователя
    private String getResponse(String text, Long chatId, String name) {
        String response;
        if (text.matches(regex)) {
            if (personService.findByEmail(text).isPresent()) {
                if (userTelegramService.isUserEmailEmpty(chatId)) {
                    //Получение email из текста сообщения и обновление ТГ пользователя
                    UserTelegram userTelegram = userTelegramService.findById(chatId);
                    userTelegram.setEmail(text);
                    Person person = personService.findByEmail(text).get();
                    userTelegram.setPersonMainService(person);
                    userTelegram.setRole(person.getRole());
                    userTelegramService.save(userTelegram);
                    response = "Email успешно добавлен, теперь доступны другие функции бота";
                } else {
                    response = String.format("%s, email уже был добавлен!", name);
                }
            } else {
                response = "Вижу, что вы указали email, при этом не удалось найти зарегистрированного " +
                        "пользователя в приложении \"Время профессионалов\". Ссылку на регистрацию можете" +
                        " попросить у вашего специалиста";
            }
        } else {
            response = "Не понимать тебя";
        }
        return response;
    }

    // регистрация пользователя телеграм
    private void registerUser(Long chatId, Chat chat) {
        if (userTelegramService.isUserNotFoundById(chatId)) {
            UserTelegram userTelegram = new UserTelegram();
            userTelegram.setChatId(chatId);
            userTelegram.setFirstName(chat.getFirstName());
            userTelegram.setLastName(chat.getLastName());
            userTelegram.setPersonusername(chat.getUserName());
            userTelegramService.save(userTelegram);
        }
    }

    //Формируем объект, который содержит ответ увидит клиент
    private SendMessage getReceiveMessage(Long chatId, String response) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(response);
        return sendMessage;
    }

    private SendMessage getReceiveMessage(Long chatId, String response, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(response);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
