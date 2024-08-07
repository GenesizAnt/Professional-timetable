package ru.genesizant.Professional.Timetable.services.telegram;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class MainMessageService {

    //ToDo сайт с кодами смайлов тут https://emojipedia.org/smiling-face-with-smiling-eyes
    //ToDo вынести все сообщения и смайлы в отдельный класс констант или только смайлы?
    //ToDo сделать абстрактный класс, от которого делать разные сервисы? Стратовое меню/Уведомления/Запись??

    @Value("${email.regexp}")
    private String regex;

    private final PersonService personService;
    private final UserTelegramService userTelegramService;

    //Получает и обрабатывает сообщение отправленное в бот
    public SendMessage messageReceiver(Update update) {
//        ReplyKeyboardMarkup keyboardMarkup = getReplyKeyboardMarkup(); //ToDo добавляет клавиатуру к каждому сообщению, оно вообще надо?
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();
            Integer messageId = update.getMessage().getMessageId();

            log.info("Получено сообщение: " + text + " , от " + chatId + " " + name );

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
                        response = EmojiParser.parseToUnicode(String.format("Привет %s! Чем могу помочь? %s", name, ":blush:"));
                    }
                }
                case "/changepassword" -> {
                    UserTelegram user = userTelegramService.findById(chatId);
                    response = "";
                    if (user == null) {
                        response = EmojiParser.parseToUnicode(String.format("Вы еще не зарегистрированы! В меню нажмите /start %s", ":blush:"));
                    } else {
                        if (user.isAgree()) {
                            response = EmojiParser.parseToUnicode(String.format("%s, чтобы изменить пароль в новом сообщении" +
                                    " укажите \"Пароль (пароль, который хотите установить)\" %s", name, ":blush:"));
                        }
                        if (!user.isAgree()) {
                            response = EmojiParser.parseToUnicode(String.format("%s, Ваше подключение к боту еще не подтверждено. " +
                                    "Для подтверждения перейдите на страницу \"Мой профиль\" в основном приложении  %s", name, ":blush:"));
                        }
                    }
                }
                default -> response = getResponse(text, chatId, name, messageId);
            }
            return getReceiveMessage(chatId, response);
        }
        return getReceiveMessage(update.getMessage().getChatId(), "Неизвестный запрос");
    }

    //Обработка разных сценариев неизвестного сообщения от пользователя
    private String getResponse(String text, Long chatId, String name, Integer messageId) {
        String response;
        if (text.matches(regex)) {
            Optional<Person> personByEmail = personService.findByEmail(text.toLowerCase());
            if (personByEmail.isPresent()) {
                if (userTelegramService.isUserEmailEmpty(chatId)) {
                    //Получение email из текста сообщения и обновление ТГ пользователя
                    UserTelegram userTelegram = userTelegramService.findById(chatId);
                    userTelegram.setEmail(text);
                    Person person = personByEmail.get();
                    userTelegram.setPersonMainService(person);
                    userTelegram.setRole(person.getRole());
                    userTelegramService.save(userTelegram);
                    response = "Email успешно добавлен, теперь доступны уведомления";
                } else {
                    response = String.format("%s, email уже был добавлен!", name);
                }
            } else {
                response = "Вижу, что вы указали email, при этом не удалось найти зарегистрированного " +
                        "пользователя в приложении \"Время профессионалов\". Ссылку на регистрацию можете" +
                        " попросить у вашего специалиста";
            }
        } else if (text.startsWith("Пароль")) {
            personService.setNewPassword(text, userTelegramService.findById(chatId).getPersonMainService());
            response = "Пароль изменен и зашифрован" + messageId;
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

////    @Scheduled(cron = "0  /15 *  *   *   * ") // 0 0 * * * * будет запускаться каждый час
//    private void sendNotifyReminderAppointment() {
//        /*
//        * пока нашел такое решение
//        * добавить к таблице встреч SpecialistAppointments две колонки Увед24часа Увед3часа - тру/фолс
//        * каждый ЧАС брать все встречи после текущей даты на каждого человека
//        * Фильтровать встречи за сутки от встречи и за три часа от встречи, с диапазоном 1,5 часа
//        * если попадает, то отправлять сообщение*/
//
//
//        LocalDate localDate = LocalDate.now().plusDays(1);
//        LocalDate localDate2 = LocalDate.now();
//
//        // Список Клиент->Его встречи
//        Map<Long, List<SpecialistAppointments>> map2 = new HashMap<>();
//        Iterable<UserTelegram> all = userTelegramService.findAll();
//
//
//        //СЕЙЧАС МЕТОД ВОЗВРАЩАЕТ ВСЕ ПОСЕЩЕНИЯ ПОСЛЕ ТЕКУЩЕГО ДНЯ!!!!!!!!!!!!!!!!!
//        for (UserTelegram userTelegram : all) {
//            map2.put(userTelegram.getChatId(), specialistAppointmentsService.
//                    findVisitorAppointmentsAfterDateWithNotifications
//                            (userTelegram.getPersonMainService().getId(), localDate2, Boolean.FALSE, Boolean.FALSE));
//        }
//
//        System.out.println();
//
//        /*
//        * Код ниже делает фильтр встреч конкретного человека по диапазону за сутки и полтора часа плюс минус
//        * нужно добавить установку флага и его проверку, что уведомление уже было или изначально делать запрос с метками фалс
//        * добавить такой же метод для уведомления за три часа*/
//
//
//            //проверка за 3 часа
//            List<SpecialistAppointments> appointmentsList = map2.get(242381592L);
//            List<SpecialistAppointments> filteredAppointments = new ArrayList<>();
//            // Получение текущей даты и времени
//            LocalDate currentDate = LocalDate.now();
//            LocalTime currentTime = LocalTime.now();
//            // Определение временных границ для фильтрации (на 24 часа раньше и на 1.5 часа вперед)
//            LocalTime lowerBound = currentTime.minusHours(3).minusMinutes(30);
//            LocalTime upperBound = currentTime.minusHours(2).minusMinutes(30);
//            // Фильтрация списка по заданным критериям
//            for (SpecialistAppointments appointmentsList1 : appointmentsList) {
//                LocalDate appointmentDate = appointmentsList1.getVisitDate();
//                LocalTime appointmentTime = appointmentsList1.getAppointmentTime();
//                // Проверка, что дата встречи раньше текущей на 24 часа и время попадает в заданный диапазон
//                if (currentDate.isEqual(appointmentDate) && appointmentTime.isAfter(lowerBound) && appointmentTime.isBefore(upperBound)) {
//                    filteredAppointments.add(appointmentsList1);
//                    //ДОБАВИТЬ ЗАПИСЬ ЧТО УВЕДОМЛЕНИЕ БЫЛООООООООООООООООООООООООООООООО!
//                }
//            }
//
//
//
//            //проверка за 24 часа
//            List<SpecialistAppointments> appointmentsList2 = map2.get(242381592L);
//            List<SpecialistAppointments> filteredAppointments2 = new ArrayList<>();
//            // Получение текущей даты и времени
//            LocalDate currentDate2 = LocalDate.now();
//            LocalTime currentTime2 = LocalTime.now();
//            // Определение временных границ для фильтрации (на 24 часа раньше и на 1.5 часа вперед)
//            LocalTime lowerBound2 = currentTime2.minusHours(1).minusMinutes(30);
//            LocalTime upperBound2 = currentTime2.plusHours(1).plusMinutes(30);
//            // Фильтрация списка по заданным критериям
//            for (SpecialistAppointments appointmentsList1 : appointmentsList2) {
//                LocalDate appointmentDate = appointmentsList1.getVisitDate();
//                LocalTime appointmentTime = appointmentsList1.getAppointmentTime();
//                // Проверка, что дата встречи раньше текущей на 24 часа и время попадает в заданный диапазон
//                if (currentDate2.isEqual(appointmentDate.minusDays(1)) && appointmentTime.isAfter(lowerBound2) && appointmentTime.isBefore(upperBound2)) {
//                    filteredAppointments2.add(appointmentsList1);
//                    //ДОБАВИТЬ ЗАПИСЬ ЧТО УВЕДОМЛЕНИЕ БЫЛООООООООООООООООООООООООООООООО!
//                }
//            }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////        List<SpecialistAppointments> appointmentsList = map.get(255720333L);
////            List<SpecialistAppointments> filteredAppointments = new ArrayList<>();
////            // Получение текущей даты и времени
////            LocalDate currentDate = LocalDate.now();
////            LocalTime currentTime = LocalTime.now();
////            // Определение временных границ для фильтрации (на 24 часа раньше и на 1.5 часа вперед)
////            LocalTime lowerBound = currentTime.minusHours(1).minusMinutes(30);
////            LocalTime upperBound = currentTime.plusHours(1).plusMinutes(30);
////            // Фильтрация списка по заданным критериям
////            for (SpecialistAppointments appointmentsList1: appointmentsList) {
//////                for (SpecialistAppointments appointment : appointmentsList1) {
////                    LocalDate appointmentDate = appointmentsList1.getVisitDate();
////                    LocalTime appointmentTime = appointmentsList1.getAppointmentTime();
////                    // Проверка, что дата встречи раньше текущей на 24 часа и время попадает в заданный диапазон
////                    if (currentDate.isEqual(appointmentDate.minusDays(1)) && appointmentTime.isAfter(lowerBound) && appointmentTime.isBefore(upperBound)) {
////                        filteredAppointments.add(appointmentsList1);
////                    }
//////                }
////            }
//
//    }
}
