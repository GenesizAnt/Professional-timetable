package ru.genesizant.Professional.Timetable.controllers.specialist;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/specialist")
public class SpecialistController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;

    @Autowired
    public SpecialistController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
    }

    //Отображение меню специалиста, что он может делать
    @GetMapping("/start_menu_specialist")
    public String getStartMenu(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            model.addAttribute("name", request.getSession().getAttribute("name"));

//            List<Person> specialists = personService.getPersonByRoleList("ROLE_ADMIN");

            model.addAttribute("name", request.getSession().getAttribute("name"));
//            model.addAttribute("specialists", specialists);
//            (long) request.getSession().getAttribute("id")


        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "specialist/start_menu_specialist";
    }

    //Отображение календаря для специалиста
//    @GetMapping("/admission_calendar_view")
//    public String addAdmissionCalendarView(Model model, HttpServletRequest request) {
//
//        if (jwtUtil.isValidJWTAndSession(request)) {
//
//            Map<LocalDate, Map<String, String>> sortedFreeSchedule = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));
//
//            model.addAttribute("name", request.getSession().getAttribute("name"));
//            model.addAttribute("dates", sortedFreeSchedule);
//
//        } else {
//            model.addAttribute("error", "Упс! Пора перелогиниться!");
//            return "redirect:/auth/login?error";
//        }
//
//        return "specialist/admission_calendar_view";
//    }

}
