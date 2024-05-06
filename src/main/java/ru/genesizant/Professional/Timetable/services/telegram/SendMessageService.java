package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.genesizant.Professional.Timetable.enums.StatusPerson.SPECIALIST;
import static ru.genesizant.Professional.Timetable.enums.StatusPerson.VISITOR;

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

//    public SendMessage notifyCancellation(SendMessage sendMessage) {
//        return sendMessage;
//    }

    public void getNotifyCancellationMsg(StatusPerson statusPerson, LocalDateTime localDateTime, Long specialistId) {
        SpecialistAppointments appointmentsSpecificDay = specialistAppointmentsService.getAppointmentsSpecificDay(specialistId, localDateTime);
        try {
            switch (statusPerson) {
                case VISITOR -> telegramBot.execute(createMessage(userTelegramService.
                                findBySpecialistId(appointmentsSpecificDay.getSpecialistAppointments().getId()).getChatId(),
                        cancellationMessage(appointmentsSpecificDay, VISITOR)));
                case SPECIALIST -> telegramBot.execute(createMessage(userTelegramService.
                                findBySpecialistId(appointmentsSpecificDay.getVisitorAppointments().getId()).getChatId(),
                        cancellationMessage(appointmentsSpecificDay, SPECIALIST)));
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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
