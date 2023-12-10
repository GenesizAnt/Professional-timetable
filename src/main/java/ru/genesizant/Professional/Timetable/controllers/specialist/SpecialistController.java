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
    @GetMapping("/admission_calendar_view")
    public String addAdmissionCalendarView(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            Map<LocalDate, Map<String, String>> datesNotSorted = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));



            // Создаем новую Map для хранения отсортированных значений
            Map<LocalDate, Map<String, String>> datesNoSortedDate = new LinkedHashMap<>();

            // Сортировка значений внутренней Map и вставка их в отсортированную Map
            datesNotSorted.forEach((key, value) -> { //ToDo Эти сортировки должен делать класс Сервиса!!!!
                Map<String, String> sortedInnerMap = value.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
                datesNoSortedDate.put(key, sortedInnerMap);
            });

            // Сортировка ключей Map и вставка их в отсортированную Map
            Map<LocalDate, Map<String, String>> sortedDates = datesNoSortedDate.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

            model.addAttribute("name", request.getSession().getAttribute("name"));
            model.addAttribute("dates", sortedDates);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "specialist/admission_calendar_view";
    }

}
