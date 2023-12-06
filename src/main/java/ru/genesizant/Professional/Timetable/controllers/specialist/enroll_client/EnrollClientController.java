package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/enroll")
public class EnrollClientController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;

    @Autowired
    public EnrollClientController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService, SpecialistsAndClientService specialistsAndClientService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
        this.specialistsAndClientService = specialistsAndClientService;
    }

    @GetMapping("/enroll_page")
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {

//            <!--Здесь форма для отображения списка закрепленных за специалистом ЗАРЕГ клиентов-->
            List<PersonFullName> clientsBySpecialist = specialistsAndClientService.getClientsBySpecialistList((long) request.getSession().getAttribute("id"));
            model.addAttribute("clientsBySpecialist", clientsBySpecialist);
            //ToDo форма не закончена сделать контролер для приема выбранного клиента

//            <!--Напротив по горизонтали форма для отображения списка закрепленных за специалистом НЕ_ЗАРЕГ клиентов-->
//            <!--Под ней поле для создания НЕ_ЗАРЕГ клиента-->
//            <!--Еще немного ниже поле для сопоставления ЗАРЕГ и НЕ_ЗАРЕГ клиентов-->

//            <!--Выбор времени и даты для регистрации клиента-->

//            <!--Отображение календаря-->
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
