package ru.genesizant.Professional.Timetable.controllers.specialist.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.enums.StatusAdmissionTime;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/calendar")
public class CalendarManagementController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final ObjectMapper objectMapper;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ERROR_VALIDATE_FORM = "redirect:/calendar/admission_calendar_view?error=";
    private final String CALENDAR_VIEW_REDIRECT = "redirect:/calendar/admission_calendar_view";

    @Autowired
    public CalendarManagementController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
        this.objectMapper = objectMapper;
    }

    //Отображение страницы управление календарем (создание расписания - доступного/не доступного времени)
    @GetMapping("/admission_calendar_view")
    public String addAdmissionCalendarView(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Перешел на страницу управления календарем");
        } else {
            model.addAttribute("error", "Упс! Непредвиденная ошибка. Переоткройте, пожалуйста приложение");
            return ERROR_LOGIN;
        }
        return "specialist/admission_calendar_view";
    }

    // Кнопка для автоматического заполнения календаря на будущий период
    @PostMapping("/admission_calendar_update")
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request,
                                             @RequestParam("startDate") String startDate,
                                             @RequestParam("endDate") String endDate,
                                             @RequestParam("startTime") String startTime,
                                             @RequestParam("endTime") String endTime,
                                             @RequestParam("minInterval") String minInterval) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        Person personSpecialist = findLoggedInPerson(request);

        if (isValidFormAddCalendarAdmission(startDate, endDate, startTime, endTime, minInterval)) {
            if (datesAppointmentsService.isDateWithinRangeOfAppointments(startDate, endDate, personSpecialist.getId())) {
                return encodeError("Нельзя добавить календарь в уже существующих датах");
            } else {
                datesAppointmentsService.addFreeDateSchedule(personSpecialist,
                                                            startDate,
                                                            endDate,
                                                            startTime,
                                                            endTime,
                                                            minInterval,
                                                            StatusAdmissionTime.AVAILABLE);
                displayPage(model, request);
                log.info("Спец: " + request.getSession().getAttribute("id") + ". Нажал кнопку для автоматического заполнения календаря на будущий период");
            }
        } else {
            return encodeError("Для создания календаря нужно внести все данные по форме");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить полный День из доступных для выбора дат
    @PostMapping("/dateFormDelete")
    public String selectedDateFormDelete(Model model, HttpServletRequest request,
                                         @RequestParam("selectedDate") Optional<LocalDate> selectedDate) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (selectedDate.isPresent()) {
            datesAppointmentsService.deleteVisitDate(selectedDate.get());
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Удалил полный день " + selectedDate.get());
        } else {
            return encodeError("Для удаления нужно выбрать дату");
        }
        return CALENDAR_VIEW_REDIRECT;
    }


    //Удалить Диапазон Дней из доступных для выбора дат
    @PostMapping("/dateRangeFormDelete")
    public String selectedDateRangeFormDelete(Model model, HttpServletRequest request,
                                              @RequestParam("startDateRange") Optional<LocalDate> startDateRange,
                                              @RequestParam("endDateRange") Optional<LocalDate> endDateRange) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (startDateRange.isPresent() && endDateRange.isPresent()) {
            datesAppointmentsService.deleteByVisitDateBetween(startDateRange.get(), endDateRange.get());
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Удалил диапазон дней из доступных для выбора дат");
        } else {
            return encodeError("Для удаления нужно выбрать диапазон дат");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить конкретное Время из доступного дня
    @PostMapping("/timeAdmissionFormDelete")
    public String selectedTimeAdmissionFormDelete(Model model, HttpServletRequest request,
                                                  @RequestParam("selectedTimeAdmission") String selectedTimeAdmission,
                                                  @RequestParam("dateOne") Optional<LocalDate> dateOne) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (!selectedTimeAdmission.equals("") && dateOne.isPresent()) {
            datesAppointmentsService.deleteTimeAdmission(dateOne.get(), selectedTimeAdmission);
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Удалил конкретное Время из доступного дня");
        } else {
            return encodeError("Для удаления нужно выбрать дату и время");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить диапазон Времени из доступного дня
    @PostMapping("/timeAdmissionRangeFormDelete")
    public String selectedTimeAdmissionRangeFormDelete(Model model, HttpServletRequest request,
                                                       @RequestParam("startTimeAdmission") String startTimeAdmission,
                                                       @RequestParam("endTimeAdmission") String endTimeAdmission,
                                                       @RequestParam("dateRange") Optional<LocalDate> dateOne) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (!startTimeAdmission.equals("") && !endTimeAdmission.equals("") && dateOne.isPresent()) {
            datesAppointmentsService.deleteTimeRangeAdmission(dateOne.get(), startTimeAdmission, endTimeAdmission);
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Удалил диапазон времени из доступного дня");
        } else {
            return encodeError("Для удаления нужно выбрать дату и время");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Изменить статус доступности конкретного Времени из календаря доступного для выбора
    @PostMapping("/setTimeAvailabilityStatus")
    public String setTimeAvailabilityStatus(Model model, HttpServletRequest request,
                                            @RequestParam("date") Optional<LocalDate> date,
                                            @RequestParam("timeAdmission") String timeAdmission,
                                            @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (isValidSetTimeForm(date, timeAdmission, status)) {
            datesAppointmentsService.setStatusTimeAdmission(date.get(), timeAdmission, status);
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Изменил статус доступности конкретного Времени из календаря доступного для выбора");
        } else {
            return encodeError("Чтобы установить доступность времени нужно выбрать Время, Дату и Статус");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Изменить статус доступности Диапазона Времени из календаря доступного для выбора
    @PostMapping("/setRangeTimeAvailabilityStatus")
    public String setRangeTimeAvailabilityStatus(Model model, HttpServletRequest request,
                                                 @RequestParam("date") Optional<LocalDate> date,
                                                 @RequestParam("startStartAdmission") String startStartAdmission,
                                                 @RequestParam("endStartAdmission") String endStartAdmission,
                                                 @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (isValidSetRangeTimeForm(date, startStartAdmission, endStartAdmission, status)) {
            datesAppointmentsService.setStatusRangeTimeAdmission(date.get(), startStartAdmission, endStartAdmission, status);
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Изменил статус доступности Диапазона Времени из календаря доступного для выбора");
        } else {
            return encodeError("Чтобы установить доступность времени нужно выбрать Время, Дату и Статус");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить все Даты из календаря до указанной даты
    @PostMapping("/dateBeforeClear")
    public String dateBeforeClear(Model model, HttpServletRequest request,
                                  @RequestParam("selectedDateBeforeClear") Optional<LocalDate> date) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (date.isPresent()) {
            datesAppointmentsService.clearDateVisitBefore(date.get());
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Удалил все даты из календаря до указанной даты - " + date.get());
        } else {
            return encodeError("Нужно выбрать дату, ДО которой будет очистка календаря");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Добавить в календарь конкретное время, дату и статус доступное для приема
    @PostMapping("/addTimeAvailability")
    public String addTimeAvailability(Model model, HttpServletRequest request,
                                      @RequestParam("timeAvailability") String timeAvailability,
                                      @RequestParam("dateAddTime") Optional<LocalDate> date,
                                      @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (isValidFormAddTimeAvailability(date, timeAvailability, status)) {
            datesAppointmentsService.addTimeAvailability(date.get(), timeAvailability, status);
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Добавил в календарь конкретное время, дату и статус доступное для приема");
        } else {
            return encodeError("Нужно выбрать дату, время и статус времени приема");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Добавить в календарь Диапазон времени, дату и статус доступное для приема
    @PostMapping("/addRangeTimeAvailability")
    public String addRangeTimeAvailability(Model model, HttpServletRequest request,
                                           @RequestParam("startAddTimeAdmission") String startTimeAvailability,
                                           @RequestParam("endAddTimeAdmission") String endTimeAvailability,
                                           @RequestParam("minIntervalAdd") String intervalHour,
                                           @RequestParam("dateRangeAdd") Optional<LocalDate> date,
                                           @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (isValidFormAddRangeTimeAvailability(date, startTimeAvailability, endTimeAvailability, status, intervalHour)) {
            datesAppointmentsService.addRangeTimeAvailability(date.get(), startTimeAvailability, endTimeAvailability, intervalHour, status);
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Добавил в календарь Диапазон времени, дату и статус доступное для приема");
        } else {
            return encodeError("Нужно выбрать дату, время, статус и интервал времени приема");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    private void displayPage(Model model, HttpServletRequest request) {
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
    }

    private Person findLoggedInPerson(HttpServletRequest request) {
        return Optional.ofNullable((Long) request.getSession().getAttribute("id"))
                .flatMap(personService::findById)
                .orElseThrow(() -> new RuntimeException("Person not found"));
    }

    private String encodeError(String error) {
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }

    private boolean isValidFormAddCalendarAdmission(String startDate, String endDate, String startTime, String endTime, String minInterval) {
        return !startDate.equals("") && !endDate.equals("") && !startTime.equals("") && !endTime.equals("") && !minInterval.equals("");
    }

    private boolean isValidSetTimeForm(Optional<LocalDate> date, String timeAdmission, StatusAdmissionTime status) {
        return timeAdmission.equals("") && !status.getStatus().equals("") && date.isPresent();
    }

    private boolean isValidSetRangeTimeForm(Optional<LocalDate> date, String startStartAdmission, String endStartAdmission, StatusAdmissionTime status) {
        return startStartAdmission.equals("") && endStartAdmission.equals("") && !status.getStatus().equals("") && date.isPresent();
    }

    private boolean isValidFormAddTimeAvailability(Optional<LocalDate> date, String timeAvailability, StatusAdmissionTime status) {
        return date.isPresent() && !timeAvailability.equals("") && !status.getStatus().equals("");
    }
    private boolean isValidFormAddRangeTimeAvailability(Optional<LocalDate> date, String startTimeAvailability, String endTimeAvailability, StatusAdmissionTime status, String intervalHour) {
        return date.isPresent() && !startTimeAvailability.equals("") && !endTimeAvailability.equals("") && !status.getStatus().equals("") && !intervalHour.equals("");
    }
}
