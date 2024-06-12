package ru.genesizant.Professional.Timetable.controllers.mng.specialist;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/specialist")
public class SpecialistController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }

    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
//        if (jwtUtil.isValidJWTInRun(request)) {
            return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
//        } else {
//            log.error("Ошибка валидации JWT токена у пользователя - " + request.getSession().getAttribute("id"));
//            throw new JWTVerificationException("");
//        }
    }

    //Отображение меню специалиста, что он может делать
    @GetMapping("/start_menu_specialist")
    public String getStartMenu(@ModelAttribute("specialist") Person specialist) {

//        if (jwtUtil.isValidJWTAndSession(request)) {
//            model.addAttribute("name", request.getSession().getAttribute("name"));
//            log.info("Спец: " + specialist.getFullName() + ". Перешел на страницу отображения меню специалиста");
//        } else {
//            model.addAttribute("error", "Упс! Пора перелогиниться!");
//            return "redirect:/auth/login?error";
//        }

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
