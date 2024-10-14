package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.repositories.VacantSeatRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        LocalDate startDateObject = formatDate(startDate);
        LocalDate endDateObject = formatDate(endDate);

        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);

        List<String> availableRecordingTime = availableRecordingTime(
                startTime != null ? startTime : startDate,
                endTime != null ? endTime : endDate, minInterval);

        for (int i = 0; i <= daysBetween; i++) {
            for (String s : availableRecordingTime) {
                if (!weekDays.contains(getRusDayWeek(startDateObject.plusDays(i).getDayOfWeek().name()))) {
                    VacantSeat vacantSeat = new VacantSeat();
                    vacantSeat.setDayOfWeek(getRusDayWeekShort(startDateObject.plusDays(i).getDayOfWeek().name()));
                    vacantSeat.setDateVacant(startDateObject.plusDays(i));
                    vacantSeat.setTimeVacant(LocalTime.parse(s));
                    vacantSeat.setSpecId(specialist);
                    vacantSeatRepository.save(vacantSeat);
                }
            }
        }
    }

    private List<String> availableRecordingTime(String startTimeWork, String endTimeWork, String timeIntervalHour) {
        LocalTime startTime = formatTime(startTimeWork);
        LocalTime endTime = formatTime(endTimeWork);

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

    public boolean isDateWithinRangeOfAppointments(String startDate, String endDate, Person id) {
        List<VacantSeat> allVisitDatesBySpecialistId = vacantSeatRepository.findBySpecId(id);
        LocalDate startDateObject = formatDate(startDate);
        LocalDate endDateObject = formatDate(endDate);
        for (VacantSeat datesAppointments : allVisitDatesBySpecialistId) {
            LocalDate date = datesAppointments.getDateVacant();
            if (date.isEqual(startDateObject) || date.isEqual(endDateObject)
                    || (date.isAfter(startDateObject) && date.isBefore(endDateObject))) {
                return true;
            }
        }
        return false;
    }

    public List<VacantSeat> getVacantSeats(Person specialist) {
        List<VacantSeat> bySpecId = vacantSeatRepository.findBySpecId(specialist);

        return sortVacantSeatsByDateTimeAsc(bySpecId);
    }

    private List<VacantSeat> sortVacantSeatsByDateTimeAsc(List<VacantSeat> vacantSeats) {
        // Сортировка списка по возрастанию по дате и времени
        return vacantSeats.stream()
                .sorted(Comparator.comparing(VacantSeat::getDateVacant).thenComparing(VacantSeat::getTimeVacant))
                .collect(Collectors.toList());
    }

    public void removeVacantSlot(String id) {
        vacantSeatRepository.deleteById(Long.valueOf(id));
    }

    public Page<VacantSeat> findAll(Pageable pageable) {
        return vacantSeatRepository.findAll(pageable);
    }

    public Page<VacantSeat> findAllVacantSeatForVisitor(Long idVisitor, Pageable pageable) {
        return vacantSeatRepository.findByIdVisitor(idVisitor, pageable);
    }

    public Page<VacantSeat> getVacantSeatsPage(Person specialist, Pageable pageable) {
        return vacantSeatRepository.findByDateVacantGreaterThanEqualAndSpecId(LocalDate.now(), specialist, pageable);
    }

    public Page<VacantSeat> getVacantSeatsPageVisitor(Long idVisitor, Pageable pageable) {
        return vacantSeatRepository.findVacantSeatsByDateAndIdVisitor(
                LocalDate.now(), idVisitor, pageable);
    }

    public void addTimeAvailability(Person spec, LocalDate date, LocalTime time) {
        VacantSeat vacantSeat = new VacantSeat();
        vacantSeat.setDayOfWeek(getRusDayWeekShort(date.getDayOfWeek().name()));
        vacantSeat.setDateVacant(date);
        vacantSeat.setTimeVacant(time);
        vacantSeat.setSpecId(spec);
        vacantSeatRepository.save(vacantSeat);
    }

    public void deleteVisitDate(Person specialist, LocalDate selectedDate) {
        vacantSeatRepository.deleteByDateAndSpecId(selectedDate, specialist.getId());
    }

    public VacantSeat findById(Long vacantSeatId) {
        return vacantSeatRepository.findById(vacantSeatId).orElseThrow();
    }

    public void save(VacantSeat vacantSeat) {
        vacantSeatRepository.save(vacantSeat);
    }
    private LocalDate formatDate(String inputDate) {
        LocalDate dateObject;
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm MM/dd/yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDateTime dateTime = LocalDateTime.parse(inputDate, formatter1);
            dateObject = dateTime.toLocalDate();
        } catch (DateTimeParseException e1) {
            try {
                dateObject = LocalDate.parse(inputDate, formatter2);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid date format: " + inputDate);
            }
        }
        return dateObject;
    }

    private LocalTime formatTime(String inputTime) {
        LocalTime timeObject;
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
        try {
            timeObject = LocalTime.parse(inputTime.split(" ")[0], formatter2);
        } catch (DateTimeParseException e1) {
            try {
                timeObject = LocalTime.parse(inputTime, formatter2);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid time format: " + inputTime);
            }
        }
        return timeObject;
    }
}
