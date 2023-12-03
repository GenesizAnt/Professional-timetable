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
            datesAppointmentsService.addFreeDateSchedule(personSpecialist.get(),startDate, endDate, startTime, endTime, minInterval);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }
    }

    @PostMapping("/selectedDate")
    public String handleDateSubmission(HttpServletRequest request, @RequestParam("selectedDate") LocalDate selectedDate) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            datesAppointmentsService.deleteVisitDate(selectedDate);

            return "redirect:/specialist/admission_calendar_view";

        } else {
            return "redirect:/auth/login?error";
        }
    }


//    @PostMapping("/calendar/selectedDateRange")
//    public String handleDateSubmission(HttpServletRequest request, @RequestParam("selectedDate") LocalDate selectedDate) {
//        if (jwtUtil.isValidJWTAndSession(request)) {
//
//
//
//            return "redirect:/specialist/admission_calendar_view";
//
//        } else {
//            return "redirect:/auth/login?error";
//        }
//    }
}
