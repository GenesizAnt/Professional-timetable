package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotifyMessageService {

    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final UserTelegramService userTelegramService;;

    public NotifyMessageService(SpecialistAppointmentsService specialistAppointmentsService, UserTelegramService userTelegramService) {
        this.specialistAppointmentsService = specialistAppointmentsService;
        this.userTelegramService = userTelegramService;
    }

    public List<SendMessage> messageReceiver() {
        return new ArrayList<>(sendNotifyReminderAppointment());
    }

    private List<SendMessage> sendNotifyReminderAppointment() {
        List<SendMessage> messages = new ArrayList<>();
        messages.addAll(getSendMessages(meetingsWithoutNotificationsInRange24Hour(), 24));
        messages.addAll(getSendMessages(meetingsWithoutNotificationsInRange3Hour(), 3));
        return messages;
    }

    private List<SendMessage> getSendMessages(Map<Long, List<SpecialistAppointments>> inRange, int hour) {
        List<SendMessage> messages = new ArrayList<>();
        for (Map.Entry<Long, List<SpecialistAppointments>> entry : inRange.entrySet()) {
            Long chatId = entry.getKey();
            List<SpecialistAppointments> appointments = entry.getValue();
            for (SpecialistAppointments appointment : appointments) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText(getMessNotify(appointment));
                messages.add(message);
                changeFlagNotify(hour, appointment);
            }
        }
        return messages;
    }

    private void changeFlagNotify(int hour, SpecialistAppointments appointment) {
        if (hour == 24) {
            SpecialistAppointments specialistAppointments = specialistAppointmentsService.findById(appointment.getId());
            specialistAppointments.setNotify24hours(Boolean.TRUE);
            specialistAppointmentsService.save(specialistAppointments);
        } else if (hour == 3) {
            SpecialistAppointments specialistAppointments = specialistAppointmentsService.findById(appointment.getId());
            specialistAppointments.setNotify3hours(Boolean.TRUE);
            specialistAppointmentsService.save(specialistAppointments);
        }
    }

    private String getMessNotify(SpecialistAppointments appointment) {
        return String.format("%s, напоминаем, что у Вас назначена встреча на %s в %s со специалистом %s",
                appointment.getVisitorAppointments().getUsername(),
                appointment.getVisitDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                appointment.getSpecialistAppointments().getFullName());
    }

    private Map<Long, List<SpecialistAppointments>> meetingsWithoutNotificationsInRange24Hour() {
        Map<Long, List<SpecialistAppointments>> personAppointments = meetingsWithoutNotifications(Boolean.FALSE, Boolean.FALSE);
        if (isAnyListNotEmpty(personAppointments)) {
            Map<Long, List<SpecialistAppointments>> meetingsWithoutNotificationsInRange24Hour = new HashMap<>();

            for (Long person : personAppointments.keySet()) {

                List<SpecialistAppointments> appointmentsWithoutNotifications = new ArrayList<>();
                List<SpecialistAppointments> appointmentsList = personAppointments.get(person);

                // Фильтрация списка по заданным критериям
                for (SpecialistAppointments appointment : appointmentsList) {
                    if (isNeedNotifyBy24Hour(appointment)) {
                        appointmentsWithoutNotifications.add(appointment);
                    }
                }
                meetingsWithoutNotificationsInRange24Hour.put(person, appointmentsWithoutNotifications);
            }
            return meetingsWithoutNotificationsInRange24Hour;
        } else {
            return Map.of();
        }
    }

    private Map<Long, List<SpecialistAppointments>> meetingsWithoutNotificationsInRange3Hour() {
        Map<Long, List<SpecialistAppointments>> personAppointments = meetingsWithoutNotifications(Boolean.TRUE, Boolean.FALSE);
        if (isAnyListNotEmpty(personAppointments)) {
            Map<Long, List<SpecialistAppointments>> meetingsWithoutNotificationsInRange3Hour = new HashMap<>();

            for (Long person : personAppointments.keySet()) {

                List<SpecialistAppointments> appointmentsWithoutNotifications = new ArrayList<>();
                List<SpecialistAppointments> appointmentsList = personAppointments.get(person);

                // Фильтрация списка по заданным критериям
                for (SpecialistAppointments appointment : appointmentsList) {
                    if (isNeedNotifyBy3Hour(appointment)) {
                        appointmentsWithoutNotifications.add(appointment);
                    }
                }
                meetingsWithoutNotificationsInRange3Hour.put(person, appointmentsWithoutNotifications);
            }
            return meetingsWithoutNotificationsInRange3Hour;
        } else {
            return Map.of();
        }
    }

    private boolean isAnyListNotEmpty(Map<Long, List<SpecialistAppointments>> map) {
        for (List<SpecialistAppointments> appointmentsList : map.values()) {
            if (!appointmentsList.isEmpty()) {
                return true; // Если хотя бы один список не пустой, возвращаем true
            }
        }
        return false; // Если все списки пусты, возвращаем false
    }

    // Проверка, что дата встречи раньше текущей на 24 часа и время попадает в заданный диапазон
    private boolean isNeedNotifyBy24Hour(SpecialistAppointments appointment) {
        LocalDate currentDate = LocalDate.now();
        LocalTime lowerBound = LocalTime.now().minusHours(0).minusMinutes(30);
        LocalTime upperBound = LocalTime.now().plusHours(0).plusMinutes(30);
        LocalDate appointmentDate = appointment.getVisitDate();
        LocalTime appointmentTime = appointment.getAppointmentTime();
        return currentDate.isEqual(appointmentDate.minusDays(1)) && appointmentTime.isAfter(lowerBound) && appointmentTime.isBefore(upperBound);
    }

    // Проверка, что дата встречи раньше текущей на 3 часа и время попадает в заданный диапазон
    private boolean isNeedNotifyBy3Hour(SpecialistAppointments appointment) {
        LocalDate currentDate = LocalDate.now();
        LocalDate appointmentDate = appointment.getVisitDate();
        LocalTime appointmentTimeLowerBound = appointment.getAppointmentTime().minusHours(3).minusMinutes(30);
        LocalTime appointmentTimeUpperBound = appointment.getAppointmentTime().minusHours(2).minusMinutes(30);
        return currentDate.isEqual(appointmentDate) && LocalTime.now().isAfter(appointmentTimeLowerBound) && LocalTime.now().isBefore(appointmentTimeUpperBound);
    }

    // Список Клиент->Его встречи, которые будут
    private Map<Long, List<SpecialistAppointments>> meetingsWithoutNotifications(Boolean isNotifyOneDay, Boolean isNotifyThreeHours) {
        LocalDate localDate = LocalDate.now();
        Map<Long, List<SpecialistAppointments>> personAppointments = new HashMap<>();
        Iterable<UserTelegram> all = userTelegramService.findAll();
        for (UserTelegram userTelegram : all) {
            personAppointments.put(userTelegram.getChatId(), specialistAppointmentsService.
                    findVisitorAppointmentsAfterDateWithNotifications
                            (userTelegram.getPersonMainService().getId(), localDate, isNotifyOneDay, isNotifyThreeHours));
        }
        return personAppointments;
    }

}
