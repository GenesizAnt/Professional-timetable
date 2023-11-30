package ru.genesizant.Professional.Timetable.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.DatesAppointments;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.DatesAppointmentsRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DatesAppointmentsService {

    private final DatesAppointmentsRepository datesAppointmentsRepository;

    @Autowired
    public DatesAppointmentsService(DatesAppointmentsRepository datesAppointmentsRepository) {
        this.datesAppointmentsRepository = datesAppointmentsRepository;
    }

    public void addFreeDateSchedule(Person personSpecialist, String startDate, String endDate, String startTimeWork, String endTimeWork, String timeIntervalHour) {
        // Создаем объект LocalDate для начала даты
        LocalDate startDateObject = LocalDate.parse(startDate);
        // Создаем объект LocalDate для конца даты
        LocalDate endDateObject = LocalDate.parse(endDate);
        // Вычисляем количество дней между двумя датами
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject) + 1; //ToDo есть ли решение лучше

        Map<String, String> availableRecordingTime = availableRecordingTime(startTimeWork, endTimeWork, timeIntervalHour);

        for (int i = 0; i < daysBetween; i++) {
//            DatesAppointments datesAppointments = new DatesAppointments(id, startDateObject, createScheduleJSON(availableRecordingTime));
//            datesAppointments.s
            datesAppointmentsRepository.save(new DatesAppointments(personSpecialist, startDateObject, createScheduleJSON(availableRecordingTime)));
        }

    }

    private Map<String, String> availableRecordingTime (String startTimeWork, String endTimeWork, String timeIntervalHour) {
        LocalTime startTime = LocalTime.parse(startTimeWork);
        LocalTime endTime = LocalTime.parse(endTimeWork);
        LocalTime timeString = LocalTime.parse(timeIntervalHour);
        int intervalMinutes = timeString.getHour() * 60 + timeString.getMinute();

        List<String> timeList = new ArrayList<>();
        LocalTime currentTime = startTime;
        while (currentTime.isBefore(endTime)) {
            timeList.add(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }

        String[] timeArray = timeList.toArray(new String[0]);
        Map<String, String> availableScheduleTime = new HashMap<>();
        for (String time : timeArray) {
            availableScheduleTime.put(time, "доступно"); //ToDo Сделать энам состояние для расписания. Свободен занят под вопросом
        }
        return availableScheduleTime;
    }

    public Map<LocalDate, String> getCalendarFreeScheduleById(long id) {
        List<DatesAppointments> allById = datesAppointmentsRepository.findAllBySpecialistDateAppointmentsIdOrderById(id);
        Map<LocalDate, String> freeSchedule = new HashMap<>();
        for (DatesAppointments datesAppointments : allById) {
            freeSchedule.put(datesAppointments.getVisitDate(), datesAppointments.getScheduleTime());
        }
        System.out.println();
        return freeSchedule;
    }

    private String createScheduleJSON(Map<String, String> availableRecordingTime) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(availableRecordingTime);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
