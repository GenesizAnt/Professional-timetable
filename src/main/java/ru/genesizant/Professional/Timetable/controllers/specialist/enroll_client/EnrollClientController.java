package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor;
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

import static ru.genesizant.Professional.Timetable.enums.StatusPerson.SPECIALIST;
import static ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor.REGISTERED;
import static ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor.UNREGISTERED;

@Controller
@RequestMapping("/enroll")
public class EnrollClientController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final ModelMapper modelMapper;
    private final UnregisteredPersonService unregisteredPersonService;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ERROR_VALIDATE_FORM = "redirect:/enroll/enroll_page?error=";
    private final String ENROLL_VIEW_REDIRECT = "specialist/enroll_client_view";

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
    public String addAdmissionCalendarUpdate(Model model, HttpServletRequest request) { //ToDo showEnrollPage
        if (jwtUtil.isValidJWTAndSession(request)) {

            displayPage(model, request);

        } else {
            return ERROR_LOGIN;
        }

        return ENROLL_VIEW_REDIRECT;
    }

    //Выбор и отображение Клиента для записи
    @PostMapping("/customerForRecording")
    public String customerForRecording(Model model, HttpServletRequest request, //ToDo chooseCustomerForRecording
                                       @RequestParam("clientFullName") Optional<@NotNull String> clientId,
                                       @RequestParam("registeredStatus") StatusRegisteredVisitor registeredStatus) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (clientId.isPresent() && !clientId.get().equals("")) {

            handleCustomerSelection(model, clientId, registeredStatus);
            displayPage(model, request);

        } else {
            return encodeError("Для работы с клиентом нужно сначала его выбрать!");
        }
        return ENROLL_VIEW_REDIRECT;
    }

    //Специалист создает незарегистрированного пользователя
    @PostMapping("/newUnregisteredPerson")
    public String newUnregisteredPerson(Model model, HttpServletRequest request, //ToDo addNewUnregisteredPerson
                                        @RequestParam("username") String username,
                                        @RequestParam("surname") String surname,
                                        @RequestParam("patronymic") String patronymic) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (isValidPersonInformation(username, surname, patronymic)) {
            unregisteredPersonService.addNewUnregisteredPerson(username, surname, patronymic, personService.findById((long) request.getSession().getAttribute("id")).get());
            displayPage(model, request);
        } else {
            return encodeError("Чтобы создать незарегистрированного в приложении клиента нужно указать ФИО полностью");
        }

        return ENROLL_VIEW_REDIRECT;
    }

    //Запись клиента на выбранную дату
    @PostMapping("/newDatesAppointments")
    public String newDatesAppointments(Model model, HttpServletRequest request,
                                       @RequestParam("meeting") Optional<LocalDateTime> meeting,
                                       @RequestParam("selectedCustomerId") String selectedCustomerId,
                                       @RequestParam("registeredStatus") Optional<StatusRegisteredVisitor> registeredStatus) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (isValidMeetingRequestParameters(meeting, selectedCustomerId, registeredStatus)) {
            //ToDo сделать Валид для проверки что время не забронированно

            if (registeredStatus.get().equals(REGISTERED)) {
                PersonFullName personFullName = modelMapper.map(personService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meeting.get(), personFullName, (long) request.getSession().getAttribute("id"), SPECIALIST);
            } else if (registeredStatus.get().equals(UNREGISTERED)) {
                PersonFullName personFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meeting.get(), personFullName, (long) request.getSession().getAttribute("id"), SPECIALIST);
            }
            //ToDo сделать добавление в БД Таблица Приемы
            displayPage(model, request);

        } else {
            return encodeError("Для записи нужно выбрать клиента, время и дату записи");
        }

        return ENROLL_VIEW_REDIRECT;
    }

    //Специалист отменяет запись ранее записанного клиента
    @PostMapping("/cancellingBooking")
    public String cancellingBooking(Model model, HttpServletRequest request,
                                    @RequestParam("meetingCancel") Optional<@NotNull LocalDateTime> meetingCancel) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (meetingCancel.isPresent()) {
            datesAppointmentsService.cancellingBookingAppointments(meetingCancel.get(), (long) request.getSession().getAttribute("id"));
            displayPage(model, request);
        } else {
            return encodeError("Для отмены записи нужно выбрать КЛИЕНТА и ДАТУ отмены приема");
        }

        return ENROLL_VIEW_REDIRECT;
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

    private void handleCustomerSelection(Model model, Optional<@NotNull String> clientId, StatusRegisteredVisitor registeredStatus) {
        if (registeredStatus.equals(REGISTERED)) {
            PersonFullName personFullNameRegistered = modelMapper.map(personService.findById(Long.valueOf(clientId.get())), PersonFullName.class);
            model.addAttribute("selectedCustomerFullName", personFullNameRegistered);
            model.addAttribute("selectedCustomerId", personFullNameRegistered.getId());
            model.addAttribute("registeredStatus", REGISTERED);
        } else if (registeredStatus.equals(UNREGISTERED)) {
            PersonFullName personFullNameUnregistered = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(clientId.get())), PersonFullName.class);
            model.addAttribute("selectedCustomerFullName", personFullNameUnregistered);
            model.addAttribute("selectedCustomerId", personFullNameUnregistered.getId());
            model.addAttribute("registeredStatus", UNREGISTERED);
        }
    }

    private String encodeError(String error) {
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }

    private boolean isValidPersonInformation(String username, String surname, String patronymic) {
        return !username.isEmpty() && !surname.isEmpty() && !patronymic.isEmpty();
    }

    private boolean isValidMeetingRequestParameters(Optional<LocalDateTime> meeting, String selectedCustomerId, Optional<StatusRegisteredVisitor> registeredStatus) {
//        if (meeting.isPresent() && !selectedCustomerId.equals("") && registeredStatus.isPresent())
        return meeting.isPresent() && !selectedCustomerId.equals("") && registeredStatus.isPresent();
    }

    //ToDo сделать форму для сопоставления ЗАРЕГ и НЕ_ЗАРЕГ клиентов
}
