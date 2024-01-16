package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;
import ru.genesizant.Professional.Timetable.services.UnregisteredPersonService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client.StatusRegisteredPerson.REGISTERED;
import static ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client.StatusRegisteredPerson.UNREGISTERED;

@Controller
@RequestMapping("/enroll")
public class EnrollClientController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final ModelMapper modelMapper;
    private final UnregisteredPersonService unregisteredPersonService;

    @Autowired
    public EnrollClientController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService, SpecialistsAndClientService specialistsAndClientService, ModelMapper modelMapper, UnregisteredPersonService unregisteredPersonService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
        this.specialistsAndClientService = specialistsAndClientService;
        this.modelMapper = modelMapper;
        this.unregisteredPersonService = unregisteredPersonService;
    }

    //Отображение страницы для Записи Клиента
    //передает на Вью - Имя спеца, Список зарег и незарег пользователей, Календарь
    @GetMapping("/enroll_page")
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            displayPage(model, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "specialist/enroll_client_view";
    }


    //Выбор и отображение Клиента для записи
    @PostMapping("/customerForRecording")
    public String customerForRecording(Model model, HttpServletRequest request,
                                       @RequestParam("clientFullName") String clientId,
                                       @RequestParam("registeredStatus") StatusRegisteredPerson registeredStatus) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            if (registeredStatus.equals(REGISTERED)) {
                PersonFullName personFullNameRegistered = modelMapper.map(personService.findById(Long.valueOf(clientId)), PersonFullName.class);
                model.addAttribute("selectedCustomerFullName", personFullNameRegistered);
                model.addAttribute("selectedCustomerId", personFullNameRegistered.getId());
                model.addAttribute("registeredStatus", REGISTERED);
            } else if (registeredStatus.equals(UNREGISTERED)) {
                PersonFullName personFullNameUnregistered = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(clientId)), PersonFullName.class);
                model.addAttribute("selectedCustomerFullName", personFullNameUnregistered);
                model.addAttribute("selectedCustomerId", personFullNameUnregistered.getId());
                model.addAttribute("registeredStatus", UNREGISTERED);
            }

            displayPage(model, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "specialist/enroll_client_view";
    }

    //Специалист создает незарегистрированного пользователя
    @PostMapping("/newUnregisteredPerson")
    public String newUnregisteredPerson(Model model, HttpServletRequest request,
                                        @RequestParam("username") String username,
                                        @RequestParam("surname") String surname,
                                        @RequestParam("patronymic") String patronymic) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            unregisteredPersonService.addNewUnregisteredPerson(username, surname, patronymic, personService.findById((long) request.getSession().getAttribute("id")).get());

            displayPage(model, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }
        return "specialist/enroll_client_view";
    }

    //Запись клиента на выбранную дату
    @PostMapping("/newDatesAppointments")
    public String newDatesAppointments(Model model, HttpServletRequest request,
                                       @RequestParam("meeting") LocalDateTime meeting,
                                       @RequestParam("selectedCustomerId") String selectedCustomerId,
                                       @RequestParam("registeredStatus") StatusRegisteredPerson registeredStatus) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            //ToDo сделать Валид для проверки что время не забронированно
            if (registeredStatus.equals(REGISTERED)) {
                PersonFullName personFullName = modelMapper.map(personService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meeting, personFullName, (long) request.getSession().getAttribute("id"));
            } else if (registeredStatus.equals(UNREGISTERED)) {
                PersonFullName personFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meeting, personFullName, (long) request.getSession().getAttribute("id"));
            }
            //ToDo сделать добавление в БД Таблица Приемы

            displayPage(model, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login";
        }
        return "specialist/enroll_client_view";
    }

    //Специалист отменяет запись ранее записанного клиента
    @PostMapping("/cancellingBooking")
    public String cancellingBooking(Model model, HttpServletRequest request,
                                    @RequestParam("meetingCancel") Optional<@NotNull LocalDateTime> meetingCancel) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            if (meetingCancel.isPresent()) {
                datesAppointmentsService.cancellingBookingAppointments(meetingCancel.get(), (long) request.getSession().getAttribute("id"));
                displayPage(model, request);
            } else {
                String error = "Для отмены записи нужно выбрать КЛИЕНТА и ДАТУ отмены приема";
                return "redirect:/enroll/enroll_page?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
            }

        } else {
            return "redirect:/auth/login"; //ToDo добавить сообщение, что пора перелогиниться
        }

        return "specialist/enroll_client_view";
    }

    //Получение и передача данных для отображения страницы
    private void displayPage(Model model, HttpServletRequest request) {
        List<PersonFullName> clientsBySpecialist = specialistsAndClientService.getClientsBySpecialistList((long) request.getSession().getAttribute("id"));
        List<UnregisteredPerson> unregisteredBySpecialist = unregisteredPersonService.getUnregisteredPersonBySpecialistList((long) request.getSession().getAttribute("id"));
        Map<LocalDate, Map<String, String>> sortedFreeSchedule = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));

        model.addAttribute("clientsBySpecialist", clientsBySpecialist);
        model.addAttribute("unregisteredBySpecialist", unregisteredBySpecialist);
        model.addAttribute("name", request.getSession().getAttribute("name"));
        model.addAttribute("dates", sortedFreeSchedule);
    }

    //ToDo сделать форму для сопоставления ЗАРЕГ и НЕ_ЗАРЕГ клиентов
}
