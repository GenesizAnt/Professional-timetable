package ru.genesizant.Professional.Timetable.services.telegram;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    //ToDo сайт с кодами смайлов тут https://emojipedia.org/smiling-face-with-smiling-eyes
    //ToDo вынести все сообщения и смайлы в отдельный класс констант или только смайлы?
    //ToDo сделать абстрактный класс, от которого делать разные сервисы? Стратовое меню/Уведомления/Запись??

    @Value("${email.regexp}")
    private String regex;

    private final PersonService personService;
    private final UserTelegramService userTelegramService;
    private final SpecialistAppointmentsService specialistAppointmentsService;

    @Autowired
    public MessageService(PersonService personService, UserTelegramService userTelegramService, SpecialistAppointmentsService specialistAppointmentsService) {
        this.personService = personService;
        this.userTelegramService = userTelegramService;
        this.specialistAppointmentsService = specialistAppointmentsService;
    }

    //Получает и обрабатывает сообщение отправленное в бот
    public SendMessage messageReceiver(Update update) {
        ReplyKeyboardMarkup keyboardMarkup = getReplyKeyboardMarkup(); //ToDo добавляет клавиатуру к каждому сообщению, оно вообще надо?
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();

            //ДОАБВЛЕНО ДЛЯ ТЕСТА, НУЖНО ВКЛЮЧИТЬ РАСПИСАНИЕ!!!!!!!!!!!
            sendNotifyReminderAppointment();

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
            if (personService.findByEmail(text.toLowerCase()).isPresent()) {
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

//    @Scheduled(cron = "0 * * * * *") // 0 0 * * * * будет запускаться каждый час
    private void sendNotifyReminderAppointment() {
        /*
        * пока нашел такое решение
        * добавить к таблице встреч SpecialistAppointments две колонки Увед24часа Увед3часа - тру/фолс
        * каждый ЧАС брать все встречи после текущей даты на каждого человека
        * Фильтровать встречи за сутки от встречи и за три часа от встречи, с диапазоном 1,5 часа
        * если попадает, то отправлять сообщение*/


        LocalDate localDate = LocalDate.now().minusDays(1);

        // Список Клиент->Его встречи
        Map<Long, List<SpecialistAppointments>> map = new HashMap<>();
        Iterable<UserTelegram> all = userTelegramService.findAll();


        //СЕЙЧАС МЕТОД ВОЗВРАЩАЕТ ВСЕ ПОСЕЩЕНИЯ ПОСЛЕ ТЕКУЩЕГО ДНЯ!!!!!!!!!!!!!!!!!
        for (UserTelegram userTelegram : all) {
            map.put(userTelegram.getChatId(), specialistAppointmentsService.findVisitorAppointmentsAfterDate(userTelegram.getPersonMainService().getId(), localDate));
        }



        /*
        * Код ниже делает фильтр встреч конкретного человека по диапазону за сутки и полтора часа плюс минус
        * нужно добавить установку флага и его проверку, что уведомление уже было или изначально делать запрос с метками фалс
        * добавить такой же метод для уведомления за три часа*/

        List<SpecialistAppointments> appointmentsList = map.get(255720333L);
            List<SpecialistAppointments> filteredAppointments = new ArrayList<>();
            // Получение текущей даты и времени
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            // Определение временных границ для фильтрации (на 24 часа раньше и на 1.5 часа вперед)
            LocalTime lowerBound = currentTime.minusHours(1).minusMinutes(30);
            LocalTime upperBound = currentTime.plusHours(1).plusMinutes(30);
            // Фильтрация списка по заданным критериям
            for (SpecialistAppointments appointmentsList1: appointmentsList) {
//                for (SpecialistAppointments appointment : appointmentsList1) {
                    LocalDate appointmentDate = appointmentsList1.getVisitDate();
                    LocalTime appointmentTime = appointmentsList1.getAppointmentTime();
                    // Проверка, что дата встречи раньше текущей на 24 часа и время попадает в заданный диапазон
                    if (currentDate.isEqual(appointmentDate.minusDays(1)) && appointmentTime.isAfter(lowerBound) && appointmentTime.isBefore(upperBound)) {
                        filteredAppointments.add(appointmentsList1);
                    }
//                }
            }







    }
}
