package ru.genesizant.Professional.Timetable.controllers.specialist.calendar;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.time.LocalDate;
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

    //прием формы для автоматического заполнение календаря на будущий период
    @PostMapping("/admission_calendar_update")
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request,
                                             @RequestParam("startDate") String startDate,
                                             @RequestParam("endDate") String endDate,
                                             @RequestParam("startTime") String startTime,
                                             @RequestParam("endTime") String endTime,
                                             @RequestParam("minInterval") String minInterval) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            Optional<Person> personSpecialist = personService.findById((long) request.getSession().getAttribute("id"));

            if (datesAppointmentsService.isBetweenSavedDate(startDate, endDate, personSpecialist.get().getId())) {
                //ToDo добавить отображение ошибки - нельзя добавить к уже существующим данным
            } else {
                datesAppointmentsService.addFreeDateSchedule(personSpecialist.get(), startDate, endDate, startTime, endTime, minInterval, StatusAdmissionTime.AVAILABLE); //ToDo будет ли ошибка если ввести даты или время наоборот
            }



            return "redirect:/specialist/admission_calendar_view";

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }
    }

    //удалить полный День из календаря доступных для выбора дат
    @PostMapping("/dateFormDelete")
    public String selectedDateFormDelete(HttpServletRequest request, @RequestParam("selectedDate") LocalDate selectedDate) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            datesAppointmentsService.deleteVisitDate(selectedDate);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }


    //удалить Диапазон Дней из календаря доступных для выбора дат
    @PostMapping("/dateRangeFormDelete")
    public String selectedDateRangeFormDelete(HttpServletRequest request,
                                              @RequestParam("startDateRange") LocalDate startDateRange,
                                              @RequestParam("endDateRange") LocalDate endDateRange) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            datesAppointmentsService.deleteByVisitDateBetween(startDateRange, endDateRange);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }

    //удалить конкретное Время из календаря доступное для выбора
    @PostMapping("/timeAdmissionFormDelete")
    public String selectedTimeAdmissionFormDelete(HttpServletRequest request,
                                                  @RequestParam("selectedTimeAdmission") String selectedTimeAdmission,
                                                  @RequestParam("dateOne") LocalDate dateOne) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            datesAppointmentsService.deleteTimeAdmission(dateOne, selectedTimeAdmission);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }

    //удалить Диапазон Времени из календаря доступного для выбора
    @PostMapping("/timeAdmissionRangeFormDelete")
    public String selectedTimeAdmissionRangeFormDelete(HttpServletRequest request,
                                                       @RequestParam("startTimeAdmission") String startTimeAdmission,
                                                       @RequestParam("endTimeAdmission") String endTimeAdmission,
                                                       @RequestParam("dateRange") LocalDate dateOne) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            datesAppointmentsService.deleteTimeRangeAdmission(dateOne, startTimeAdmission, endTimeAdmission);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }

    //изменить статус доступности конкретного Времени из календаря доступного для выбора
    @PostMapping("/setTimeAvailabilityStatus")
    public String setTimeAvailabilityStatus(HttpServletRequest request,
                                            @RequestParam("date") LocalDate date,
                                            @RequestParam("timeAdmission") String timeAdmission,
                                            @RequestParam("selectedOption") StatusAdmissionTime status,
                                            Model model) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            if (status == null) {
//                model.addAttribute("error", "Необходимо выбрать статус времени"); //ToDo сделать отображение ошибки
//                return "your-error-view"; // отобразить страницу с сообщением об ошибке
            }

            datesAppointmentsService.setStatusTimeAdmission(date, timeAdmission, status);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }


    //изменить статус доступности Диапазона Времени из календаря доступного для выбора
    @PostMapping("/setRangeTimeAvailabilityStatus")
    public String setRangeTimeAvailabilityStatus(HttpServletRequest request,
                                                 @RequestParam("date") LocalDate date,
                                                 @RequestParam("startStartAdmission") String startStartAdmission,
                                                 @RequestParam("endStartAdmission") String endStartAdmission,
                                                 @RequestParam("selectedOption") StatusAdmissionTime status,
                                                 Model model) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            if (status == null) {
//                model.addAttribute("error", "Необходимо выбрать статус времени"); //ToDo сделать отображение ошибки
//                return "your-error-view"; // отобразить страницу с сообщением об ошибке
            }

            datesAppointmentsService.setStatusRangeTimeAdmission(date, startStartAdmission, endStartAdmission, status);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }

    //Удалить все Даты из календаря до указанной даты
    @PostMapping("/dateBeforeClear")
    public String dateBeforeClear(HttpServletRequest request, @RequestParam("selectedDateBeforeClear") LocalDate date) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            datesAppointmentsService.clearDateVisitBefore(date);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
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

            return "redirect:/specialist/admission_calendar_view";

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

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }
}
