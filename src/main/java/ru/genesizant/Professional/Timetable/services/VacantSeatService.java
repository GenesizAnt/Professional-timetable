package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.repositories.VacantSeatRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ru.genesizant.Professional.Timetable.enums.DayOfWeekRus.getRusDayWeek;

@Service
@RequiredArgsConstructor
public class VacantSeatService {

    private final VacantSeatRepository vacantSeatRepository;

    public void addFreeDateSchedule(Person specialist, String startDate, String endDate, String startTime, String endTime, String minInterval, List<String> weekDays) {
        // Создаем объект LocalDate для начала даты
        LocalDate startDateObject = LocalDate.parse(startDate);
        // Создаем объект LocalDate для конца даты
        LocalDate endDateObject = LocalDate.parse(endDate);
        // Вычисляем количество дней между двумя датами
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);

        List<String> availableRecordingTime = availableRecordingTime(startTime, endTime, minInterval);

        for (int i = 0; i < daysBetween; i++) {
            for (String s : availableRecordingTime) {
                if (!weekDays.contains(getRusDayWeek(startDateObject.plusDays(i).getDayOfWeek().name()))) {
                    VacantSeat vacantSeat = new VacantSeat();
                    vacantSeat.setDayOfWeek(getRusDayWeek(startDateObject.plusDays(i).getDayOfWeek().name()));
                    vacantSeat.setDate_vacant(startDateObject.plusDays(i));
                    vacantSeat.setTime_vacant(LocalTime.parse(s));
                    vacantSeat.setSpecId(specialist);
                    vacantSeatRepository.save(vacantSeat);
                }
            }
        }
    }

    private List<String> availableRecordingTime(String startTimeWork, String endTimeWork, String timeIntervalHour) {
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
        return timeList;
    }

    public List<VacantSeat> getVacantSeats(Person specialist) {
        return vacantSeatRepository.findBySpecId(specialist);
    }

//    public void addFreeDateSchedule(Person personSpecialist, String startDate, String endDate, String startTimeWork, String endTimeWork, String timeIntervalHour, StatusAdmissionTime status) {
//        // Создаем объект LocalDate для начала даты
//        LocalDate startDateObject = LocalDate.parse(startDate);
//        // Создаем объект LocalDate для конца даты
//        LocalDate endDateObject = LocalDate.parse(endDate);
//        // Вычисляем количество дней между двумя датами
//        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);
//
//        //Получить расписание в формате "Время":"Статус"
//        Map<String, String> availableRecordingTime = availableRecordingTime(startTimeWork, endTimeWork, timeIntervalHour, status);
//
//        for (int i = 0; i <= daysBetween; i++) {
//            Optional<DatesAppointments> existingRecord = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(startDateObject.plusDays(i), personSpecialist.getId());
//            if (existingRecord.isPresent()) {
//                // обновление существующей записи
//                existingRecord.get().setScheduleTime(getScheduleJSON(availableRecordingTime));
//                datesAppointmentsRepository.save(existingRecord.get());
//            } else {
//                // создание новой записи
//                DatesAppointments newRecord = new DatesAppointments(startDateObject.plusDays(i), personSpecialist, getScheduleJSON(availableRecordingTime));
//                datesAppointmentsRepository.save(newRecord);
//            }
//        }
//    }
}
