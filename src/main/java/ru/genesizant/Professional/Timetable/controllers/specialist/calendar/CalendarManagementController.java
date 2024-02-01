package ru.genesizant.Professional.Timetable.controllers.specialist.calendar;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/calendar")
public class CalendarManagementController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;

    @Autowired
    public CalendarManagementController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
    }

    @GetMapping("/admission_calendar_view")
    public String addAdmissionCalendarView(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            displayPage(model, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }
        return "specialist/admission_calendar_view";
    }

    // форма для автоматического заполнения календаря на будущий период
    @PostMapping("/admission_calendar_update")
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request,
                                             @RequestParam("startDate") String startDate,
                                             @RequestParam("endDate") String endDate,
                                             @RequestParam("startTime") String startTime,
                                             @RequestParam("endTime") String endTime,
                                             @RequestParam("minInterval") String minInterval) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        Optional<Person> personSpecialist = getLoggedInPerson(request);

        if (isValidFormAddCalendarAdmission(startDate, endDate, startTime, endTime, minInterval)) {
            if (datesAppointmentsService.isBetweenSavedDate(startDate, endDate, personSpecialist.get().getId())) {
                return encodeError("Нельзя добавить календарь в уже существующих датах");
            } else {
                datesAppointmentsService.addFreeDateSchedule(personSpecialist.get(), startDate, endDate, startTime, endTime, minInterval, StatusAdmissionTime.AVAILABLE);
                displayPage(model, request);
            }
        } else {
            return encodeError("Для создания календаря нужно внести все данные по форме");
        }
        return "specialist/admission_calendar_view";
    }

    //удалить полный День из календаря доступных для выбора дат
    @PostMapping("/dateFormDelete")
    public String selectedDateFormDelete(Model model, HttpServletRequest request,
                                         @RequestParam("selectedDate") Optional<LocalDate> selectedDate) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        if (selectedDate.isPresent()) {
            datesAppointmentsService.deleteVisitDate(selectedDate.get());
            displayPage(model, request);
        } else {
            return encodeError("Для удаления нужно выбрать дату");
        }
        return "specialist/admission_calendar_view";
    }


    //удалить Диапазон Дней из календаря доступных для выбора дат
    @PostMapping("/dateRangeFormDelete")
    public String selectedDateRangeFormDelete(Model model, HttpServletRequest request,
                                              @RequestParam("startDateRange") Optional<LocalDate> startDateRange,
                                              @RequestParam("endDateRange") Optional<LocalDate> endDateRange) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        if (startDateRange.isPresent() && endDateRange.isPresent()) {
            datesAppointmentsService.deleteByVisitDateBetween(startDateRange.get(), endDateRange.get());
            displayPage(model, request);
        } else {
            return encodeError("Для удаления нужно выбрать диапазон дат");
        }
        return "specialist/admission_calendar_view";
    }

    //удалить конкретное Время из календаря доступное для выбора
    @PostMapping("/timeAdmissionFormDelete")
    public String selectedTimeAdmissionFormDelete(Model model, HttpServletRequest request,
                                                  @RequestParam("selectedTimeAdmission") String selectedTimeAdmission,
                                                  @RequestParam("dateOne") Optional<LocalDate> dateOne) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        if (selectedTimeAdmission.equals("") && dateOne.isPresent()) {
            datesAppointmentsService.deleteTimeAdmission(dateOne.get(), selectedTimeAdmission);
            displayPage(model, request);
        } else {
            return encodeError("Для удаления нужно выбрать дату и время");
        }
        return "specialist/admission_calendar_view";
    }

    //удалить Диапазон Времени из календаря доступного для выбора
    @PostMapping("/timeAdmissionRangeFormDelete")
    public String selectedTimeAdmissionRangeFormDelete(Model model, HttpServletRequest request,
                                                       @RequestParam("startTimeAdmission") String startTimeAdmission,
                                                       @RequestParam("endTimeAdmission") String endTimeAdmission,
                                                       @RequestParam("dateRange") Optional<LocalDate> dateOne) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        if (startTimeAdmission.equals("") && endTimeAdmission.equals("") && dateOne.isPresent()) {
            datesAppointmentsService.deleteTimeRangeAdmission(dateOne.get(), startTimeAdmission, endTimeAdmission);
            displayPage(model, request);
        } else {
            return encodeError("Для удаления нужно выбрать дату и время");
        }
        return "specialist/admission_calendar_view";
    }

    //изменить статус доступности конкретного Времени из календаря доступного для выбора
    @PostMapping("/setTimeAvailabilityStatus")
    public String setTimeAvailabilityStatus(Model model, HttpServletRequest request,
                                            @RequestParam("date") Optional<LocalDate> date,
                                            @RequestParam("timeAdmission") String timeAdmission,
                                            @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        if (isValidSetTimeForm(date, timeAdmission, status)) {
            datesAppointmentsService.setStatusTimeAdmission(date.get(), timeAdmission, status);
            displayPage(model, request);
        } else {
            return encodeError("Чтобы установить доступность времени нужно выбрать Время, Дату и Статус");
        }
        return "specialist/admission_calendar_view";
    }

    //изменить статус доступности Диапазона Времени из календаря доступного для выбора
    @PostMapping("/setRangeTimeAvailabilityStatus")
    public String setRangeTimeAvailabilityStatus(Model model, HttpServletRequest request,
                                                 @RequestParam("date") Optional<LocalDate> date,
                                                 @RequestParam("startStartAdmission") String startStartAdmission,
                                                 @RequestParam("endStartAdmission") String endStartAdmission,
                                                 @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        if (isValidSetRangeTimeForm(date, startStartAdmission, endStartAdmission, status)) {
            datesAppointmentsService.setStatusRangeTimeAdmission(date.get(), startStartAdmission, endStartAdmission, status);
            displayPage(model, request);
        } else {
            return encodeError("Чтобы установить доступность времени нужно выбрать Время, Дату и Статус");
        }
        return "specialist/admission_calendar_view";
    }

    //Удалить все Даты из календаря до указанной даты
    @PostMapping("/dateBeforeClear")
    public String dateBeforeClear(Model model, HttpServletRequest request,
                                  @RequestParam("selectedDateBeforeClear") Optional<LocalDate> date) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        if (date.isPresent()) {
            datesAppointmentsService.clearDateVisitBefore(date.get());
            displayPage(model, request);
        } else {
            return encodeError("Нужно выбрать дату, ДО которой будет очистка календаря");
        }
        return "specialist/admission_calendar_view";
    }

    //Добавить в календарь конкретное время, дату и статус доступное для приема
    @PostMapping("/addTimeAvailability")
    public String addTimeAvailability(HttpServletRequest request,
                                      @RequestParam("timeAvailability") String timeAvailability,
                                      @RequestParam("dateAddTime") LocalDate date,
                                      @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            if (status == null) {
//                model.addAttribute("error", "Необходимо выбрать статус времени"); //ToDo сделать отображение ошибки
//                return "your-error-view"; // отобразить страницу с сообщением об ошибке
            }

            datesAppointmentsService.addTimeAvailability(date, timeAvailability, status);

            return "specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }

    //Добавить в календарь Диапазон времени, дату и статус доступное для приема
    @PostMapping("/addRangeTimeAvailability")
    public String addRangeTimeAvailability(HttpServletRequest request,
                                           @RequestParam("startAddTimeAdmission") String startTimeAvailability,
                                           @RequestParam("endAddTimeAdmission") String endTimeAvailability,
                                           @RequestParam("minIntervalAdd") String intervalHour,
                                           @RequestParam("dateRangeAdd") LocalDate date,
                                           @RequestParam("selectedOption") StatusAdmissionTime status) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            if (status == null) {
//                model.addAttribute("error", "Необходимо выбрать статус времени"); //ToDo сделать отображение ошибки
//                return "your-error-view"; // отобразить страницу с сообщением об ошибке
            }

            datesAppointmentsService.addRangeTimeAvailability(date, startTimeAvailability, endTimeAvailability, intervalHour, status);

            return "specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }

    private void displayPage(Model model, HttpServletRequest request) {
        Map<LocalDate, Map<String, String>> sortedFreeSchedule =
                datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));
        model.addAttribute("name", request.getSession().getAttribute("name"));
        model.addAttribute("dates", sortedFreeSchedule);
    }

    private Optional<Person> getLoggedInPerson(HttpServletRequest request) {
        return Optional.ofNullable((Long) request.getSession().getAttribute("id"))
                .flatMap(personService::findById);
    }

    private String encodeError(String error) {
        return "redirect:/calendar/admission_calendar_view?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }

    private boolean isValidFormAddCalendarAdmission(String startDate, String endDate, String startTime, String endTime, String minInterval) {
        return !startDate.equals("") && !endDate.equals("") && !startTime.equals("") && !endTime.equals("") && !minInterval.equals("");
    }

    private boolean isValidSetTimeForm(Optional<LocalDate> date, String timeAdmission, StatusAdmissionTime status) {
        return timeAdmission.equals("") && status != null && date.isPresent();
    }

    private boolean isValidSetRangeTimeForm(Optional<LocalDate> date, String startStartAdmission, String endStartAdmission, StatusAdmissionTime status) {
        return startStartAdmission.equals("") && endStartAdmission.equals("") && status != null && date.isPresent();
    }
}
