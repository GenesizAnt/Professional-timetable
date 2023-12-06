package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.controllers.specialist.calendar.StatusAdmissionTime;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/enroll")
public class EnrollClientController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;

    @Autowired
    public EnrollClientController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
    }
    
    @GetMapping("/enroll_page")
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request) {
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

        //                return "redirect:/specialist/enroll_client_view";
        return "specialist/enroll_client_view";
    }
}
