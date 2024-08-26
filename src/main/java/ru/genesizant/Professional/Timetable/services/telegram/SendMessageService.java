package ru.genesizant.Professional.Timetable.services.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static ru.genesizant.Professional.Timetable.enums.StatusPerson.SPECIALIST;
import static ru.genesizant.Professional.Timetable.enums.StatusPerson.VISITOR;

@Slf4j
@RequiredArgsConstructor
@Service
public class SendMessageService {

    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final UserTelegramService userTelegramService;
    private final TelegramBot telegramBot;

    public void notifyCancellation(StatusPerson statusPerson, LocalDateTime localDateTime, Long specialistId) {
        Optional<SpecialistAppointments> appointmentsSpecificDay = specialistAppointmentsService.getAppointmentsSpecificDay(specialistId, localDateTime);
        if (appointmentsSpecificDay.isPresent()) {
            Optional<UserTelegram> specialist = userTelegramService.findByPersonId(specialistId);
            Optional<UserTelegram> visitor = userTelegramService.findByPersonId(appointmentsSpecificDay.get().getVisitorAppointments().getId());
            try {
                switch (statusPerson) {
                    case VISITOR -> {
                        if (specialist.isPresent()) {
                            telegramBot.execute(createMessage(specialist.get().getChatId(),
                                    cancellationMessage(appointmentsSpecificDay.get(), VISITOR)));
                        }
                    }
                    case SPECIALIST -> {
                        if (visitor.isPresent()) {
                            telegramBot.execute(createMessage(visitor.get().getChatId(),
                                    cancellationMessage(appointmentsSpecificDay.get(), SPECIALIST)));
                        }
                    }
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки сообщения об отмене встречи на: " + localDateTime + " у специалиста " + specialistId);
            }
        }
    }

    public void notifyEnrollNewAppointment(StatusPerson statusPerson, VacantSeat vacantSeat, Long visitorId, Long specialistId) {
        Optional<UserTelegram> specialist = userTelegramService.findByPersonId(specialistId);
        Optional<UserTelegram> visitor = userTelegramService.findByPersonId(visitorId);
        if (visitor.isPresent() && specialist.isPresent()) {
            try {
                switch (statusPerson) {
                    case VISITOR -> {
                        telegramBot.execute(createMessage(specialist.get().getChatId(),
                                enrollNewAppointmentMessage(specialist.get(), visitor.get(),
                                        vacantSeat.getDateVacant(), vacantSeat.getTimeVacant(), VISITOR)));
                    }
                    case SPECIALIST -> {
                        telegramBot.execute(createMessage(visitor.get().getChatId(),
                                enrollNewAppointmentMessage(visitor.get(), specialist.get(),
                                        vacantSeat.getDateVacant(), vacantSeat.getTimeVacant(), SPECIALIST)));
                    }
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки сообщения о новой встрече на: " + vacantSeat + " у специалиста " + specialistId + " и клиента " + visitorId);
            }
        }
    }

    public void notifyNewClient(Person specialist, Person client) {
        Optional<UserTelegram> specialistTg = userTelegramService.findByPersonId(specialist.getId());
        if (specialistTg.isPresent()) {
            try {
                telegramBot.execute(createMessage(specialistTg.get().getChatId(), newClientMessage(specialist, client)));
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки сообщения о новом клиенте: " + client.getId() + " у специалиста " + specialist.getId());
            }
        }
    }

    private String newClientMessage(Person specialist, Person client) {
        return String.format("%s, произошла регистрация нового клиента: %s",
                specialist.getUsername(),
                client.getFullName());
    }

//    public void deleteMsgWithPassword(Integer messageId, Long chatId) {
//        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
//        try {
//            telegramBot.execute(deleteMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }

    private String enrollNewAppointmentMessage(UserTelegram recipient, UserTelegram sender, LocalDate localDate, LocalTime localTime, StatusPerson statusPerson) {
        String responseMsg = "";
        switch (statusPerson) {
            case VISITOR -> responseMsg = String.format("%s, сообщаем, что клиент %s записался на консультацию на%s %s в %s",
                    recipient.getFirstName(),
                    sender.getFirstName(),
                    getDiffDay(localDate),
                    localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    localTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            case SPECIALIST -> responseMsg = String.format("%s, сообщаем, что специалист %s подтвердил назначенную встречу на%s %s в %s",
                    recipient.getFirstName(),
                    sender.getFirstName(),
                    getDiffDay(localDate),
                    localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    localTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        return responseMsg;
    }

    private String cancellationMessage(SpecialistAppointments appointment, StatusPerson statusPerson) {
//        String responseMsg = "";
//        switch (statusPerson) {
//            case VISITOR -> responseMsg = String.format("%s, сообщаем, что клиент %s отменил назначенную встречу на%s %s в %s",
//                    appointment.getSpecialistAppointments().getUsername(),
//                    appointment.getVisitorAppointments().getFullName(),
//                    getDiffDay(appointment.getVisitDate().atStartOfDay()),
//                    appointment.getVisitDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
//                    appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
//            case SPECIALIST -> responseMsg = String.format("%s, сообщаем, что специалист %s отменил назначенную встречу на%s %s в %s",
//                    appointment.getVisitorAppointments().getUsername(),
//                    appointment.getSpecialistAppointments().getFullName(),
//                    getDiffDay(appointment.getVisitDate().atStartOfDay()),
//                    appointment.getVisitDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
//                    appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
//        }
//        return responseMsg;
        return "";
    }

    private SendMessage createMessage(Long chatId, String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(msg);
        return message;
    }

    private String getDiffDay(LocalDate localDate) {
        String day = "";
        int difference = (int) LocalDate.now().until(localDate, ChronoUnit.DAYS);
        switch (difference) {
            case 0 -> day = " сегодня";
            case 1 -> day = " завтра";
            case 2 -> day = " послезавтра";
        }
        return day;
    }
}
