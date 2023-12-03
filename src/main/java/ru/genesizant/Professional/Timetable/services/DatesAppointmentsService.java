package ru.genesizant.Professional.Timetable.services;

import com.fasterxml.jackson.core.type.TypeReference;
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

    public void deleteVisitDate(LocalDate date) {
        datesAppointmentsRepository.deleteByVisitDate(date);
    }

    public void addFreeDateSchedule(Person personSpecialist, String startDate, String endDate, String startTimeWork, String endTimeWork, String timeIntervalHour) {
        // Создаем объект LocalDate для начала даты
        LocalDate startDateObject = LocalDate.parse(startDate);
        // Создаем объект LocalDate для конца даты
        LocalDate endDateObject = LocalDate.parse(endDate);
        // Вычисляем количество дней между двумя датами
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);

        Map<String, String> availableRecordingTime = availableRecordingTime(startTimeWork, endTimeWork, timeIntervalHour);

        for (int i = 0; i <= daysBetween; i++) {

            Optional<DatesAppointments> existingRecord = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(startDateObject.plusDays(i), personSpecialist.getId());
            if (existingRecord.isPresent()) {
                // обновление существующей записи
                existingRecord.get().setScheduleTime(createScheduleJSON(availableRecordingTime));
                datesAppointmentsRepository.save(existingRecord.get());
            } else {
                // создание новой записи
                DatesAppointments newRecord = new DatesAppointments(startDateObject.plusDays(i), personSpecialist, createScheduleJSON(availableRecordingTime));
                datesAppointmentsRepository.save(newRecord);
            }

        }

    }

    private Map<String, String> availableRecordingTime(String startTimeWork, String endTimeWork, String timeIntervalHour) {
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

    public Map<LocalDate, Map<String, String>> getCalendarFreeScheduleById(long id) {
        List<DatesAppointments> allById = datesAppointmentsRepository.findAllBySpecialistDateAppointmentsIdOrderById(id);
        Map<LocalDate, Map<String, String>> freeSchedule = new HashMap<>();

        for (DatesAppointments datesAppointments : allById) {
            freeSchedule.put(datesAppointments.getVisitDate(), getAvailableTime(datesAppointments.getScheduleTime()));
        }

//        for (DatesAppointments datesAppointments : allById) {
//            freeSchedule.put(datesAppointments.getVisitDate(), datesAppointments.getScheduleTime());
//        }
        System.out.println();
        return freeSchedule;
    }

    private Map<String, String> getAvailableTime(String scheduleTime) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> freeSchedule = null;

        try {
            freeSchedule = mapper.readValue(scheduleTime, new TypeReference<>() {});

            System.out.println(freeSchedule);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
