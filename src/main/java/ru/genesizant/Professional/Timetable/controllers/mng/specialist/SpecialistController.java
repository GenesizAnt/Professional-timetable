package ru.genesizant.Professional.Timetable.controllers.mng.specialist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/specialist")
public class SpecialistController {

    private final JWTUtil jwtUtil;

    //Отображение меню специалиста, что он может делать
    @GetMapping("/start_menu_specialist")
    public String getStartMenu(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTAndSession(request)) {
            model.addAttribute("name", request.getSession().getAttribute("name"));
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Перешел на страницу отображения меню специалиста");
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
