package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.util.Optional;

@Service
public class MessageService {

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();

            String response;
            switch (text) {
                case "/start" -> {
                    if (userTelegramService.isUserNotFoundById(chatId)) {
                        registerUser(chatId, update.getMessage().getChat());
                        response = String.format("Привет %s! Для использования всех функций бота необходимо ввести email, который был " +
                                "использован в приложении \"Время профессионалов\"", name);
                    } else if (userTelegramService.isUserEmailEmpty(chatId)) {
                        response = String.format("Привет %s! Для использования всех функций бота необходимо ввести email, который был " +
                                "использован в приложении \"Время профессионалов\"", name);
                    } else {
                        response = String.format("Привет %s! Чем могу помочь?", name);
                    }
                }
                case "/stop" -> response = String.format("До свидания %s!", name);
                default -> {
                    response = getResponse(text, chatId, name);
                }
            }
            return getReceiveMessage(chatId, response);
        }
        return getReceiveMessage(update.getMessage().getChatId(), "Неизвестный запрос");
    }

    //Обработка разных сценариев неизвестного сообщения от пользователя
    private String getResponse(String text, Long chatId, String name) {
        String response;
        if (text.matches(regex)) {
            if (personService.findByEmail(text).isPresent()) {
                if (userTelegramService.isUserEmailEmpty(chatId)) {
                    //Получение email из текста сообщения и обновление ТГ пользователя
                    Optional<UserTelegram> userTelegram = userTelegramRepository.findById(chatId);
                    userTelegram.get().setEmail(text);
                    userTelegram.get().setPersonMainService(personService.findByEmail(text).get());
                    userTelegramRepository.save(userTelegram.get());
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
        if (userTelegramRepository.findById(chatId).isEmpty()) {
            UserTelegram userTelegram = new UserTelegram();
            userTelegram.setChatId(chatId);
            userTelegram.setFirstName(chat.getFirstName());
            userTelegram.setLastName(chat.getLastName());
            userTelegram.setPersonusername(chat.getUserName());
            userTelegramRepository.save(userTelegram);
        }
    }

    //Формируем объект, который содержит ответ увидит клиент
    private SendMessage getReceiveMessage(Long chatId, String response) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(response);
        return sendMessage;
    }
}
