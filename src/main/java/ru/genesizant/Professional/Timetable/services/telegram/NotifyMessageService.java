package ru.genesizant.Professional.Timetable.services.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.genesizant.Professional.Timetable.model.Reception;
import ru.genesizant.Professional.Timetable.services.ReceptionService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class NotifyMessageService {

    private final ReceptionService receptionService;
    private final UserTelegramService userTelegramService;

    public List<SendMessage> messageReceiver() {
        return new ArrayList<>(sendNotifyReminderAppointment());
    }

    private List<SendMessage> sendNotifyReminderAppointment() {
        List<SendMessage> messages = new ArrayList<>();
        messages.addAll(getSendMessages(meetingsWithoutNotificationsInRange24Hour(), 24));
        messages.addAll(getSendMessages(meetingsWithoutNotificationsInRange3Hour(), 3));
        return messages;
    }

    private List<SendMessage> getSendMessages(Map<Long, List<Reception>> inRange, int hour) {
        List<SendMessage> messages = new ArrayList<>();
        for (Map.Entry<Long, List<Reception>> entry : inRange.entrySet()) {
            Long chatId = entry.getKey();
            List<Reception> appointments = entry.getValue();
            for (Reception appointment : appointments) {
                SendMessage message = createMessage(chatId, getMessNotify(appointment));
                messages.add(message);
                changeFlagNotify(hour, appointment);
            }
        }
        return messages;
    }

    private SendMessage createMessage(Long chatId, String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(msg);
        return message;
    }

    private void changeFlagNotify(int hour, Reception appointment) {
        if (hour == 24) {
            Reception specialistAppointments = receptionService.findById(appointment.getId());
            specialistAppointments.setNotify24hours(Boolean.TRUE);
            receptionService.save(specialistAppointments);
        } else if (hour == 3) {
            Reception specialistAppointments = receptionService.findById(appointment.getId());
            specialistAppointments.setNotify3hours(Boolean.TRUE);
            receptionService.save(specialistAppointments);
        }
    }

    private String getMessNotify(Reception appointment) {
        String day = appointment.getDateVacant().equals(LocalDate.now()) ? " сегодня" : " завтра";
        return String.format("%s, напоминаем, что у Вас назначена встреча%s %s в %s со специалистом %s",
                appointment.getVisitorIdReception().getUsername(),
                day,
                appointment.getDateVacant().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                appointment.getTimeVacant().format(DateTimeFormatter.ofPattern("HH:mm")),
                appointment.getSpecIdReception().getFullName());
    }

    private Map<Long, List<Reception>> meetingsWithoutNotificationsInRange24Hour() {
        Map<Long, List<Reception>> personAppointments = meetingsWithoutNotifications(Boolean.FALSE, Boolean.FALSE);
        if (isAnyListNotEmpty(personAppointments)) {
            Map<Long, List<Reception>> meetingsWithoutNotificationsInRange24Hour = new HashMap<>();

            for (Long person : personAppointments.keySet()) {

                List<Reception> appointmentsWithoutNotifications = new ArrayList<>();
                List<Reception> appointmentsList = personAppointments.get(person);

                // Фильтрация списка по заданным критериям
                for (Reception appointment : appointmentsList) {
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

    private Map<Long, List<Reception>> meetingsWithoutNotificationsInRange3Hour() {
        Map<Long, List<Reception>> personAppointments = meetingsWithoutNotifications(Boolean.TRUE, Boolean.FALSE);
        if (isAnyListNotEmpty(personAppointments)) {
            Map<Long, List<Reception>> meetingsWithoutNotificationsInRange3Hour = new HashMap<>();

            for (Long person : personAppointments.keySet()) {

                List<Reception> appointmentsWithoutNotifications = new ArrayList<>();
                List<Reception> appointmentsList = personAppointments.get(person);

                // Фильтрация списка по заданным критериям
                for (Reception appointment : appointmentsList) {
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

    private boolean isAnyListNotEmpty(Map<Long, List<Reception>> map) {
        for (List<Reception> appointmentsList : map.values()) {
            if (!appointmentsList.isEmpty()) {
                return true; // Если хотя бы один список не пустой, возвращаем true
            }
        }
        return false; // Если все списки пусты, возвращаем false
    }

    // Проверка, что дата встречи раньше текущей на 24 часа и время попадает в заданный диапазон
    private boolean isNeedNotifyBy24Hour(Reception appointment) {
        LocalDate currentDate = LocalDate.now();
        LocalTime lowerBound = LocalTime.now().minusHours(0).minusMinutes(30);
        LocalTime upperBound = LocalTime.now().plusHours(0).plusMinutes(30);
        LocalDate appointmentDate = appointment.getDateVacant();
        LocalTime appointmentTime = appointment.getTimeVacant();
        return currentDate.isEqual(appointmentDate.minusDays(1)) && appointmentTime.isAfter(lowerBound) && appointmentTime.isBefore(upperBound);
    }

    // Проверка, что дата встречи раньше текущей на 3 часа и время попадает в заданный диапазон
    private boolean isNeedNotifyBy3Hour(Reception appointment) {
        LocalDate currentDate = LocalDate.now();
        LocalDate appointmentDate = appointment.getDateVacant();
        LocalTime appointmentTimeLowerBound = appointment.getTimeVacant().minusHours(3).minusMinutes(30);
        LocalTime appointmentTimeUpperBound = appointment.getTimeVacant().minusHours(2).minusMinutes(30);
        return currentDate.isEqual(appointmentDate) && LocalTime.now().isAfter(appointmentTimeLowerBound) && LocalTime.now().isBefore(appointmentTimeUpperBound);
    }

    // Список Клиент->Его встречи, которые будут
    private Map<Long, List<Reception>> meetingsWithoutNotifications(Boolean isNotifyOneDay, Boolean isNotifyThreeHours) {
        LocalDate localDate = LocalDate.now();
        Map<Long, List<Reception>> personAppointments = new HashMap<>();
        Iterable<UserTelegram> all = userTelegramService.findAll();
        for (UserTelegram userTelegram : all) {
            personAppointments.put(userTelegram.getChatId(), receptionService.
                    findVisitorAppointmentsAfterDateWithNotifications
                            (userTelegram.getPersonMainService().getId(), localDate, isNotifyOneDay, isNotifyThreeHours));
        }
        return personAppointments;
    }
}
