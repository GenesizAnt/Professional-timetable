package ru.genesizant.Professional.Timetable.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.controllers.specialist.calendar.StatusAdmissionTime;
import ru.genesizant.Professional.Timetable.model.DatesAppointments;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.DatesAppointmentsRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ru.genesizant.Professional.Timetable.controllers.specialist.calendar.StatusAdmissionTime.AVAILABLE;

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
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);

        Map<String, String> availableRecordingTime = availableRecordingTime(startTimeWork, endTimeWork, timeIntervalHour);

        for (int i = 0; i <= daysBetween; i++) {

            Optional<DatesAppointments> existingRecord = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(startDateObject.plusDays(i), personSpecialist.getId());
            if (existingRecord.isPresent()) {
                // обновление существующей записи
                existingRecord.get().setScheduleTime(getScheduleJSON(availableRecordingTime));
                datesAppointmentsRepository.save(existingRecord.get());
            } else {
                // создание новой записи
                DatesAppointments newRecord = new DatesAppointments(startDateObject.plusDays(i), personSpecialist, getScheduleJSON(availableRecordingTime));
                datesAppointmentsRepository.save(newRecord);
            }

        }

    }


    //ToDo сделать утилитный класс для методов этого сервиса?
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
            availableScheduleTime.put(time, AVAILABLE.getStatus());
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

    private String getScheduleJSON(Map<String, String> availableRecordingTime) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(availableRecordingTime);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteVisitDate(LocalDate date) {
        datesAppointmentsRepository.deleteByVisitDate(date); //ToDo вернуть ошибку если неверные данные
    }

    public void deleteByVisitDateBetween(LocalDate startDateRange, LocalDate endDateRange) {
        datesAppointmentsRepository.deleteByVisitDateBetween(startDateRange, endDateRange); //ToDo вернуть ошибку если неверные данные
    }

    public void deleteTimeAdmission(LocalDate date, String selectedTimeAdmission) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (time.containsKey(selectedTimeAdmission)) {
                time.remove(selectedTimeAdmission);
                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());
            } else {
                //ToDo вернуть ошибку про несуществующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    public void deleteTimeRangeAdmission(LocalDate date, String startTimeAdmission, String endTimeAdmission) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (time.containsKey(startTimeAdmission) && time.containsKey(endTimeAdmission)) { //ToDo сделать метод для проверки на входит ли заданный диапазон времени в существующее время или просто удалить все что входит в диапазон который задал юзер

                List<String> keysToRemove = new ArrayList<>();

                for (Map.Entry<String, String> entry : time.entrySet()) {
                    String key = entry.getKey();
                    if (isTimeInRange(key, startTimeAdmission, endTimeAdmission)) {
                        keysToRemove.add(key);
                    }
                }

                for (String key : keysToRemove) {
                    time.remove(key);
                }

                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());
            } else {
                //ToDo вернуть ошибку про несуществующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    private boolean isTimeInRange(String time, String startTimeAdmission, String endTimeAdmission) {
        return time.compareTo(startTimeAdmission) >= 0 && time.compareTo(endTimeAdmission) <= 0;
    }

    public void setStatusTimeAdmission(LocalDate date, String timeAdmission, StatusAdmissionTime status) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (time.containsKey(timeAdmission)) {
                time.put(timeAdmission, status.getStatus());
                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());
            } else {
                //ToDo вернуть ошибку про несуществующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }

    }
}
