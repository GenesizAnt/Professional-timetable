package ru.genesizant.Professional.Timetable.controllers.specialist.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.enums.StatusAdmissionTime;
import ru.genesizant.Professional.Timetable.model.BaseSchedule;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.services.BaseScheduleService;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.VacantSeatService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/calendar")
public class CalendarManagementController {

    private final ObjectMapper objectMapper;
    @Value("${error_login}")
    private final String CALENDAR_VIEW_REDIRECT = "redirect:/calendar/admission_calendar_view";

    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final VacantSeatService vacantSeatService;
    private final BaseScheduleService baseScheduleService;

    @ModelAttribute
    public void getPayloadPage(@ModelAttribute("specialist") Person specialist, Model model, HttpServletRequest request) {
        Map<LocalDate, Map<String, String>> schedule = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));
        List<String> allCalendar = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<LocalDate> nearestDates = schedule.keySet().stream()
                .filter(date -> !date.isBefore(now)) // исключаем даты, предшествующие текущей дате
                .sorted(Comparator.comparingLong(date -> ChronoUnit.DAYS.between(now, date))).toList();
        for (LocalDate nearestDate : nearestDates) {
            String[][] calendarForView = datesAppointmentsService.getCalendarForClient("specialist", nearestDate, schedule.get(nearestDate));
            try {
                allCalendar.add(objectMapper.writeValueAsString(calendarForView));
            } catch (Exception e) {
                log.error("Ошибка формирования JSON из календаря:" + Arrays.deepToString(calendarForView) + ". Текст сообщения - " + e.getMessage());
            }
        }

        // allCalendar каждый элемент это строковое предоставление массива данных определенного дня в формате [["23.04.2024","Вторник"],["Время","Бронь","Статус"],["10:00","---","Доступно".......
        for (int i = 0; i < allCalendar.size(); i++) {
            model.addAttribute("day" + i, allCalendar.get(i));
        }
        model.addAttribute("name", request.getSession().getAttribute("name"));

//        List<VacantSeat> vacantSeats = vacantSeatService.getVacantSeats(specialist);
//        model.addAttribute("vacantSeats", vacantSeats);


        // Обработка параметров пагинации
        int page = request.getSession().getAttribute("page") != null ? (int) request.getSession().getAttribute("page") : 0;
        int size = request.getSession().getAttribute("size") != null ? (int) request.getSession().getAttribute("size") : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateVacant").and(Sort.by("timeVacant")));

        Page<VacantSeat> vacantSeatsPage = vacantSeatService.getVacantSeatsPage(specialist, pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        vacantSeatsPage.getContent().forEach(vacantSeat -> {
            vacantSeat.setFormattedDate(vacantSeat.getDateVacant().format(formatter));
        });

        model.addAttribute("vacantSeats", vacantSeatsPage.getContent());
        model.addAttribute("page", vacantSeatsPage);

        Optional<BaseSchedule> scheduleSpecialist = baseScheduleService.getBaseScheduleSpecialist(specialist);
        scheduleSpecialist.ifPresent(baseSchedule -> model.addAttribute("schedule", baseSchedule));
    }

    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    //Отображение страницы управление календарем (создание расписания - доступного/не доступного времени)
    @GetMapping("/admission_calendar_view")
    public String addAdmissionCalendarView(@ModelAttribute("specialist") Person specialist) {
        return "specialist/admission_calendar_view";
    }

    // Кнопка для автоматического заполнения календаря на будущий период
    @PostMapping("/admission_calendar_update")
    public String addAdmissionCalendarUpdate(@ModelAttribute("specialist") Person specialist,
                                             @RequestParam("startDate") String startDate,
                                             @RequestParam("endDate") String endDate,
                                             @RequestParam(value = "startTime", required = false) String startTime,
                                             @RequestParam(value = "endTime", required = false) String endTime,
                                             @RequestParam("minInterval") String minInterval,
                                             @RequestParam(value = "weekDays", required = false) List<String> weekDays) {
        if (isValidFormAddCalendarAdmission(startDate, endDate, startTime, endTime, minInterval)) {
            if (weekDays == null || weekDays.isEmpty()) {
                weekDays = List.of(""); // Устанавливаем значение по умолчанию
            }
            vacantSeatService.addFreeDateSchedule(specialist, startDate, endDate, startTime, endTime, minInterval, weekDays);
//            if (datesAppointmentsService.isDateWithinRangeOfAppointments(startDate, endDate, specialist.getId())) {
//                return encodeError("Нельзя добавить календарь в уже существующих датах");
//            } else {
//                datesAppointmentsService.addFreeDateSchedule(specialist, startDate, endDate, startTime, endTime,
//                        minInterval,
//                        StatusAdmissionTime.AVAILABLE);
//                log.info("Спец: " + specialist.getFullName() + ". Нажал кнопку для автоматического заполнения календаря на будущий период");
//            }
        } else {
            return encodeError("Для создания календаря нужно внести все данные по форме");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    @PostMapping("/removeVacantSlot")
    public String handleTableClick(@RequestBody Map<String, String> applicationFromSpecialist) {
        vacantSeatService.removeVacantSlot(applicationFromSpecialist.get("id"));
        return CALENDAR_VIEW_REDIRECT;
    }

    @PostMapping("/add_sample_available_time")
    public String addSampleAvailableTime(@ModelAttribute("specialist") Person specialist,
                                         @RequestParam(name = "startDays") String startDays) {
        Optional<BaseSchedule> baseScheduleSpecialist = baseScheduleService.getBaseScheduleSpecialist(specialist);
        if (baseScheduleSpecialist.isPresent()) {

            List<String> weekDays = baseScheduleSpecialist.get().getWeekDays();



            vacantSeatService.addFreeDateSchedule(specialist, startDays, getEndDay(startDays, baseScheduleSpecialist),
                    baseScheduleSpecialist.get().getStartTime().toString(),
                    baseScheduleSpecialist.get().getEndTime().toString(), baseScheduleSpecialist.get().getMinInterval().toString(),
                    baseScheduleSpecialist.get().getWeekDays());
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить полный День из доступных для выбора дат
    @PostMapping("/dateFormDelete")
    public String selectedDateFormDelete(@ModelAttribute("specialist") Person specialist,
                                         @RequestParam("selectedDate") LocalDate selectedDate) {
        if (selectedDate != null) {
//            datesAppointmentsService.deleteVisitDate(selectedDate);
            vacantSeatService.deleteVisitDate(specialist, selectedDate);
            log.info("Спец: " + specialist.getFullName() + ". Удалил полный день " + selectedDate);
        } else {
            return encodeError("Для удаления нужно выбрать дату");
        }
        return CALENDAR_VIEW_REDIRECT;
    }


    //Удалить Диапазон Дней из доступных для выбора дат
//    @PostMapping("/dateRangeFormDelete")
//    public String selectedDateRangeFormDelete(@ModelAttribute("specialist") Person specialist,
//                                              @RequestParam("startDateRange") LocalDate startDateRange,
//                                              @RequestParam("endDateRange") LocalDate endDateRange) {
//        if (startDateRange != null && endDateRange != null) {
//            datesAppointmentsService.deleteByVisitDateBetween(startDateRange, endDateRange);
//            log.info("Спец: " + specialist.getFullName() + ". Удалил диапазон дней из доступных для выбора дат");
//        } else {
//            return encodeError("Для удаления нужно выбрать диапазон дат");
//        }
//        return CALENDAR_VIEW_REDIRECT;
//    }

    //Удалить конкретное Время из доступного дня
    @PostMapping("/timeAdmissionFormDelete")
    public String selectedTimeAdmissionFormDelete(@ModelAttribute("specialist") Person specialist,
                                                  @RequestParam("selectedTimeAdmission") String selectedTimeAdmission,
                                                  @RequestParam("dateOne") LocalDate dateOne) {
        if (!selectedTimeAdmission.isEmpty() && dateOne != null) {
            datesAppointmentsService.deleteTimeAdmission(dateOne, selectedTimeAdmission, specialist.getId());
            log.info("Спец: " + specialist.getFullName() + ". Удалил конкретное Время из доступного дня");
        } else {
            return encodeError("Для удаления нужно выбрать дату и время");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить диапазон Времени из доступного дня
    @PostMapping("/timeAdmissionRangeFormDelete")
    public String selectedTimeAdmissionRangeFormDelete(@ModelAttribute("specialist") Person specialist,
                                                       @RequestParam("startTimeAdmission") String startTimeAdmission,
                                                       @RequestParam("endTimeAdmission") String endTimeAdmission,
                                                       @RequestParam("dateRange") LocalDate dateOne) {
        if (!startTimeAdmission.isEmpty() && !endTimeAdmission.isEmpty() && dateOne != null) {
            datesAppointmentsService.deleteTimeRangeAdmission(dateOne, startTimeAdmission, endTimeAdmission, specialist.getId());
            log.info("Спец: " + specialist.getFullName() + ". Удалил диапазон времени из доступного дня");
        } else {
            return encodeError("Для удаления нужно выбрать дату и время");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Изменить статус доступности конкретного Времени из календаря доступного для выбора
    @PostMapping("/setTimeAvailabilityStatus")
    public String setTimeAvailabilityStatus(@ModelAttribute("specialist") Person specialist,
                                            @RequestParam("date") LocalDate date,
                                            @RequestParam("timeAdmission") String timeAdmission,
                                            @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (isValidSetTimeForm(date, timeAdmission, status)) {
            datesAppointmentsService.setStatusTimeAdmission(date, timeAdmission, status, specialist.getId());
            log.info("Спец: " + specialist.getFullName() + ". Изменил статус доступности конкретного Времени из календаря доступного для выбора");
        } else {
            return encodeError("Чтобы установить доступность времени нужно выбрать Время, Дату и Статус");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Изменить статус доступности Диапазона Времени из календаря доступного для выбора
    @PostMapping("/setRangeTimeAvailabilityStatus")
    public String setRangeTimeAvailabilityStatus(@ModelAttribute("specialist") Person specialist,
                                                 @RequestParam("date") LocalDate date,
                                                 @RequestParam("startStartAdmission") String startStartAdmission,
                                                 @RequestParam("endStartAdmission") String endStartAdmission,
                                                 @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (isValidSetRangeTimeForm(date, startStartAdmission, endStartAdmission, status)) {
            datesAppointmentsService.setStatusRangeTimeAdmission(date, startStartAdmission, endStartAdmission, status, specialist.getId());
            log.info("Спец: " + specialist.getFullName() + ". Изменил статус доступности Диапазона Времени из календаря доступного для выбора");
        } else {
            return encodeError("Чтобы установить доступность времени нужно выбрать Время, Дату и Статус");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить все Даты из календаря до указанной даты
    @PostMapping("/dateBeforeClear")
    public String dateBeforeClear(@ModelAttribute("specialist") Person specialist,
                                  @RequestParam("selectedDateBeforeClear") LocalDate date) {
        if (date != null) {
            datesAppointmentsService.clearDateVisitBefore(date);
            log.info("Спец: " + specialist.getFullName() + ". Удалил все даты из календаря до указанной даты - " + date);
        } else {
            return encodeError("Нужно выбрать дату, ДО которой будет очистка календаря");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Добавить в календарь конкретное время, дату и статус доступное для приема
    @PostMapping("/addTimeAvailability")
    public String addTimeAvailability(@ModelAttribute("specialist") Person specialist,
                                      @RequestParam("timeAvailability") String timeAvailability,
                                      @RequestParam("dateAddTime") LocalDate date,
                                      @RequestParam(value = "selectedOption", required = false) StatusAdmissionTime status) {
        if (isValidFormAddTimeAvailability(date, timeAvailability)) {
//            datesAppointmentsService.addTimeAvailability(specialist.getId(), date, timeAvailability, status);
            vacantSeatService.addTimeAvailability(specialist,
                    date,
                    LocalTime.of(Integer.parseInt(timeAvailability.split(":")[0]),
                            Integer.parseInt(timeAvailability.split(":")[1])));
            log.info("Спец: " + specialist.getFullName() + ". Добавил в календарь конкретное время, дату и статус доступное для приема");
        } else {
            return encodeError("Нужно выбрать дату, время и статус времени приема");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Добавить в календарь Диапазон времени, дату и статус доступное для приема
    @PostMapping("/addRangeTimeAvailability")
    public String addRangeTimeAvailability(@ModelAttribute("specialist") Person specialist,
                                           @RequestParam("startAddTimeAdmission") String startTimeAvailability,
                                           @RequestParam("endAddTimeAdmission") String endTimeAvailability,
                                           @RequestParam("minIntervalAdd") String intervalHour,
                                           @RequestParam("dateRangeAdd") LocalDate date,
                                           @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (isValidFormAddRangeTimeAvailability(date, startTimeAvailability, endTimeAvailability, status, intervalHour)) {
            datesAppointmentsService.addRangeTimeAvailability(
                    date, startTimeAvailability, endTimeAvailability,
                    intervalHour, status, specialist.getId());
            log.info("Спец: " + specialist.getFullName() + ". Добавил в календарь Диапазон времени, дату и статус доступное для приема");
        } else {
            return encodeError("Нужно выбрать дату, время, статус и интервал времени приема");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    @GetMapping("/vacantSeats")
    public String getVacantSeats(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        if (page < 0) {
            page = 0; // Устанавливаем на страницу 0, если индекс меньше 0
        }
        request.getSession().setAttribute("page", page);
        request.getSession().setAttribute("size", size);
        Page<VacantSeat> all = vacantSeatService.findAll(PageRequest.of(page, size));
        model.addAttribute("vacantSeats", all.getContent());
        model.addAttribute("page", all);
//        model.addAttribute("size", size);
        return CALENDAR_VIEW_REDIRECT;
    }


    private String getEndDay(String sDays, Optional<BaseSchedule> baseScheduleSpecialist) {
        LocalDate startDays = LocalDate.parse(sDays);
        return startDays.plusDays(baseScheduleSpecialist.get().getCountDays()).toString();
    }

    private String encodeError(String error) {
        String ERROR_VALIDATE_FORM = "redirect:/calendar/admission_calendar_view?error=";
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }

    private boolean isValidFormAddCalendarAdmission(String startDate, String endDate, String startTime, String endTime, String minInterval) {
        return !startDate.isEmpty() && !endDate.isEmpty() && !minInterval.isEmpty();
    }

    private boolean isValidSetTimeForm(LocalDate date, String timeAdmission, StatusAdmissionTime status) {
        return !timeAdmission.isEmpty() && !status.getStatus().isEmpty() && date != null;
    }

    private boolean isValidSetRangeTimeForm(LocalDate date, String startStartAdmission, String endStartAdmission, StatusAdmissionTime status) {
        return startStartAdmission.isEmpty() && endStartAdmission.isEmpty() && !status.getStatus().isEmpty() && date != null;
    }

    private boolean isValidFormAddTimeAvailability(LocalDate date, String timeAvailability) {
        return date != null && !timeAvailability.isEmpty();
    }

    private boolean isValidFormAddRangeTimeAvailability(LocalDate date, String startTimeAvailability, String endTimeAvailability, StatusAdmissionTime status, String intervalHour) {
        return date != null && !startTimeAvailability.isEmpty() && !endTimeAvailability.isEmpty() && !status.getStatus().isEmpty() && !intervalHour.isEmpty();
    }
}
