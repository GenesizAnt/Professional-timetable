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

    @PostMapping("/admission_calendar_update")
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request,
                                             @RequestParam("startDate") String startDate,
                                             @RequestParam("endDate") String endDate,
                                             @RequestParam("startTime") String startTime,
                                             @RequestParam("endTime") String endTime,
                                             @RequestParam("minInterval") String minInterval) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            Optional<Person> personSpecialist = personService.findById((long) request.getSession().getAttribute("id"));
            datesAppointmentsService.addFreeDateSchedule(personSpecialist.get(), startDate, endDate, startTime, endTime, minInterval); //ToDo будет ли ошибка если ввести даты или время наоборот


            return "redirect:/specialist/admission_calendar_view";

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }
    }

    @PostMapping("/dateFormDelete")
    public String selectedDateFormDelete(HttpServletRequest request, @RequestParam("selectedDate") LocalDate selectedDate) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            datesAppointmentsService.deleteVisitDate(selectedDate);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }


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
}
