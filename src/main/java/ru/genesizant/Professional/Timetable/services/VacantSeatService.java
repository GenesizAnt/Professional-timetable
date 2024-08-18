package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.repositories.VacantSeatRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.genesizant.Professional.Timetable.enums.DayOfWeekRus.getRusDayWeek;
import static ru.genesizant.Professional.Timetable.enums.DayOfWeekRus.getRusDayWeekShort;

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
                    vacantSeat.setDayOfWeek(getRusDayWeekShort(startDateObject.plusDays(i).getDayOfWeek().name()));
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
        List<VacantSeat> bySpecId = vacantSeatRepository.findBySpecId(specialist);

        return sortVacantSeatsByDateTimeAsc(bySpecId);
    }

    private List<VacantSeat> sortVacantSeatsByDateTimeAsc(List<VacantSeat> vacantSeats) {
        // Сортировка списка по возрастанию по дате и времени
        return vacantSeats.stream()
                .sorted(Comparator.comparing(VacantSeat::getDate_vacant).thenComparing(VacantSeat::getTime_vacant))
                .collect(Collectors.toList());
    }

    public void removeVacantSlot(String id) {
        vacantSeatRepository.deleteById(Long.valueOf(id));
    }

    public Page<VacantSeat> findAll(Pageable pageable) {
        return vacantSeatRepository.findAll(pageable);
    }

    public Page<VacantSeat> getVacantSeatsPage(Person specialist, Pageable pageable) {
        return vacantSeatRepository.findBySpecId(specialist, pageable);

    }
}
