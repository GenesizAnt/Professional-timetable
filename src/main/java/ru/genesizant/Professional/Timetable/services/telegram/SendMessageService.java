package ru.genesizant.Professional.Timetable.services.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static ru.genesizant.Professional.Timetable.enums.StatusPerson.SPECIALIST;
import static ru.genesizant.Professional.Timetable.enums.StatusPerson.VISITOR;

@Slf4j
@Service
public class SendMessageService {

    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final UserTelegramService userTelegramService;
    private final TelegramBot telegramBot;

    @Autowired
    public SendMessageService(SpecialistAppointmentsService specialistAppointmentsService, UserTelegramService userTelegramService, TelegramBot telegramBot) {
        this.specialistAppointmentsService = specialistAppointmentsService;
        this.userTelegramService = userTelegramService;
        this.telegramBot = telegramBot;
    }

    public void notifyCancellation(StatusPerson statusPerson, LocalDateTime localDateTime, Long specialistId) {
        SpecialistAppointments appointmentsSpecificDay = specialistAppointmentsService.getAppointmentsSpecificDay(specialistId, localDateTime);
        try {
            switch (statusPerson) {
                case VISITOR -> telegramBot.execute(createMessage(userTelegramService.findByPersonId(
                        specialistId).getChatId(),
                        cancellationMessage(appointmentsSpecificDay, VISITOR)));
                case SPECIALIST -> telegramBot.execute(createMessage(userTelegramService.findByPersonId(
                        appointmentsSpecificDay.getVisitorAppointments().getId()).getChatId(),
                        cancellationMessage(appointmentsSpecificDay, SPECIALIST)));
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения об отмене встречи на: " + localDateTime + " у специалиста " + specialistId);
        }
    }

    public void notifyEnrollNewAppointment(StatusPerson statusPerson, LocalDateTime localDateTime, Long visitorId, Long specialistId) {
        UserTelegram specialist = userTelegramService.findByPersonId(specialistId);
        UserTelegram visitor = userTelegramService.findByPersonId(visitorId);
        try {
            switch (statusPerson) {
                case VISITOR -> telegramBot.execute(createMessage(specialist.getChatId(),
                        enrollNewAppointmentMessage(specialist, visitor, localDateTime, VISITOR)));
                case SPECIALIST -> telegramBot.execute(createMessage(visitor.getChatId(),
                        enrollNewAppointmentMessage(visitor, specialist, localDateTime, SPECIALIST)));
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения о новой встрече на: " + localDateTime + " у специалиста " + specialistId + " и клиента " + visitorId);
        }
    }

    public void notifyNewClient(Person specialist, Person client) {
        UserTelegram specialistTg = userTelegramService.findByPersonId(specialist.getId());
        if (specialistTg != null) {
            try {
                telegramBot.execute(createMessage(specialistTg.getChatId(), newClientMessage(specialist, client)));
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

    private String enrollNewAppointmentMessage(UserTelegram recipient, UserTelegram sender, LocalDateTime localDateTime, StatusPerson statusPerson) {
        String responseMsg = "";
        switch (statusPerson) {
            case VISITOR -> responseMsg = String.format("%s, сообщаем, что клиент %s записался на консультацию на %s в %s",
                    recipient.getFirstName(),
                    sender.getFirstName(),
                    localDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            case SPECIALIST -> responseMsg = String.format("%s, сообщаем, что специалист %s подтвердил назначенную встречу на %s в %s",
                    recipient.getFirstName(),
                    sender.getFirstName(),
                    localDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        return responseMsg;
    }

    private String cancellationMessage(SpecialistAppointments appointment, StatusPerson statusPerson) {
        String responseMsg = "";
        switch (statusPerson) {
            case VISITOR -> responseMsg = String.format("%s, сообщаем, что клиент %s отменил назначенную встречу на %s в %s",
                    appointment.getSpecialistAppointments().getUsername(),
                    appointment.getVisitorAppointments().getFullName(),
                    appointment.getVisitDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            case SPECIALIST -> responseMsg = String.format("%s, сообщаем, что специалист %s отменил назначенную встречу на %s в %s",
                    appointment.getVisitorAppointments().getUsername(),
                    appointment.getSpecialistAppointments().getFullName(),
                    appointment.getVisitDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        return responseMsg;
    }

    private SendMessage createMessage(Long chatId, String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(msg);
        return message;
    }
}
