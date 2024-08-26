package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
//        LocalDate startDateObject = LocalDate.parse(startDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String datePartStart = startDate.split(" ")[1];
        LocalDate startDateObject = LocalDate.parse(datePartStart, formatter);

        // Создаем объект LocalDate для конца даты
//        LocalDate endDateObject = LocalDate.parse(endDate);
        String datePartEnd = endDate.split(" ")[1];
        LocalDate endDateObject = LocalDate.parse(datePartEnd, formatter);

        // Вычисляем количество дней между двумя датами
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);

        List<String> availableRecordingTime = availableRecordingTime(startTime, endTime, minInterval);

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
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String timePartStart = startTimeWork.split(" ")[0];
        LocalTime startTime = LocalTime.parse(timePartStart, timeFormatter);

        String timePartEnd = endTimeWork.split(" ")[0];
        LocalTime endTime = LocalTime.parse(timePartEnd, timeFormatter);

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
                .sorted(Comparator.comparing(VacantSeat::getDateVacant).thenComparing(VacantSeat::getTimeVacant))
                .collect(Collectors.toList());
    }

    public void removeVacantSlot(String id) {
        vacantSeatRepository.deleteById(Long.valueOf(id));
    }

    public Page<VacantSeat> findAll(Pageable pageable) {
        return vacantSeatRepository.findAll(pageable);
    }

    public Page<VacantSeat> getVacantSeatsPage(Person specialist, Pageable pageable) {
        List<VacantSeat> vacantSeats1 = getVacantSeats(specialist);


        Page<VacantSeat> bySpecId = vacantSeatRepository.findBySpecId(specialist, pageable);


//        List<VacantSeat> vacantSeats = bySpecId.getContent();
//        // Сортировка списка
//        List<VacantSeat> collect = vacantSeats1.stream()
//                .sorted(Comparator.comparing(VacantSeat::getDateVacant).thenComparing(VacantSeat::getTimeVacant)).toList();

//        vacantSeats.sort(Comparator.comparing(VacantSeat::getDate_vacant)
//                .thenComparing(VacantSeat::getTime_vacant));

        // Создание нового объекта Page с отсортированным содержимым
//        return new PageImpl<>(collect, bySpecId.getPageable(), bySpecId.getTotalElements());


//        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("date_vacant").and(Sort.by("time_vacant")));
//        Page<VacantSeat> sortedByPage = bySpecId.map(bySpecId -> {
//            List<VacantSeat> sortedList = vacantSeats.stream()
//                    .sorted(Comparator.comparing(VacantSeat::getDateVacant)
//                            .thenComparing(VacantSeat::getTimeVacant))
//                    .collect(Collectors.toList());
//            return new PageImpl<>(sortedList, pageRequest, vacantSeats.getTotalElements());
//        });

        return bySpecId;
    }

//    public Page<VacantSeat> getBookingSeatsPage(Person specialist, Pageable pageable) {
//        return vacantSeatRepository.findBySpecIdAndClientIdIsNotNull(specialist, pageable);
//    }

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
}
