package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    private final ObjectMapper objectMapper;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ERROR_VALIDATE_FORM = "redirect:/enroll/enroll_page?error=";
    private final String ENROLL_VIEW_REDIRECT = "redirect:/enroll/enroll_page";

    @Autowired
    public EnrollClientController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService, SpecialistsAndClientService specialistsAndClientService, ModelMapper modelMapper, UnregisteredPersonService unregisteredPersonService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
        this.specialistsAndClientService = specialistsAndClientService;
        this.modelMapper = modelMapper;
        this.unregisteredPersonService = unregisteredPersonService;
        this.objectMapper = objectMapper;
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

        return "specialist/enroll_client_view";
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

    @PostMapping("/newDatesAppointmentsTable")
    public String newDatesAppointmentsTable(Model model, HttpServletRequest request,
                                            @RequestBody Map<String, String> applicationFromSpecialist) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        String selectedCustomerId = applicationFromSpecialist.get("selectedCustomerId");
        String registeredStatus = applicationFromSpecialist.get("registeredStatus");
        LocalDateTime meetingDateTime = parseInLocalDataTime(applicationFromSpecialist);
//        String meetingDate = applicationFromSpecialist.get("meetingDate");
//        String meetingTime = applicationFromSpecialist.get("meetingTime");
//        LocalDate date = LocalDate.parse(meetingDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
//        LocalTime time = LocalTime.parse(meetingTime, DateTimeFormatter.ofPattern("HH:mm"));
//        LocalDateTime meeting = date.atTime(time);

        if (!selectedCustomerId.equals("") && !registeredStatus.equals("")) { //ToDo сделать другую проверку

            if (registeredStatus.equals(REGISTERED.name())) {
                PersonFullName personFullName = modelMapper.map(personService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, (long) request.getSession().getAttribute("id"), SPECIALIST);
            } else if (registeredStatus.equals(UNREGISTERED.name())) {
                PersonFullName personFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, (long) request.getSession().getAttribute("id"), SPECIALIST);
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


    @PostMapping("/recordingConfirmed")
    public String recordingConfirmed(Model model, HttpServletRequest request,
                                     @RequestBody Map<String, String> recordingIsConfirmed) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        String meetingPerson = recordingIsConfirmed.get("meetingPerson"); //для проверки
        PersonFullName personFullName = getPersonFullName(recordingIsConfirmed.get("meetingPerson"));
//        Map<String, String> fioPerson = fioParser(recordingIsConfirmed.get("meetingPerson"));
        LocalDateTime meetingDateTime = parseInLocalDataTime(recordingIsConfirmed);
        long id = (long) request.getSession().getAttribute("id"); //для проверки
        System.out.println();
        datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, (long) request.getSession().getAttribute("id"), SPECIALIST);

//        Map<String, String> identityVisitor = getIdentificationMeetingPerson(fioPerson, (long) request.getSession().getAttribute("id")); //ToDo Чтобы не проверять через ФИО добавить в таблицу ид клинета, но не отрисовывать

//        if (registeredStatus.get().equals(REGISTERED)) {
//            PersonFullName personFullName = modelMapper.map(personService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
//            datesAppointmentsService.enrollVisitorNewAppointments(meeting.get(), personFullName, (long) request.getSession().getAttribute("id"), SPECIALIST);
//        } else if (registeredStatus.get().equals(UNREGISTERED)) {
//            PersonFullName personFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
//            datesAppointmentsService.enrollVisitorNewAppointments(meeting.get(), personFullName, (long) request.getSession().getAttribute("id"), SPECIALIST);
//        }


//        (long) request.getSession().getAttribute("id")


//        if (true) {
//            datesAppointmentsService.cancellingBookingAppointments(meetingCancel.get(), (long) request.getSession().getAttribute("id"));
//            displayPage(model, request);
//        } else {
//            return encodeError("Для отмены записи нужно выбрать КЛИЕНТА и ДАТУ отмены приема");
//        }

        return ENROLL_VIEW_REDIRECT;
    }

    private PersonFullName getPersonFullName(String meetingPerson) {
        PersonFullName personFullName = new PersonFullName();
        String[] fioArray = meetingPerson.split(" ");
        if (fioArray.length == 3) {
            personFullName.setSurname(fioArray[0]);
            personFullName.setUsername(fioArray[1]);
            personFullName.setPatronymic(fioArray[2]);
        }
        return personFullName;
    }

    private Map<String, String> getIdentificationMeetingPerson(Map<String, String> fioPerson, long idSpecialist) {
        Map<String, String> identityVisitor = new HashMap<>();
        Long idPerson = personService.getPersonByFullName(fioPerson);
        Optional<SpecialistsAndClient> specialistsAndClient = specialistsAndClientService.findByVisitorListId(idPerson);
        if (specialistsAndClient.isPresent()) {
            identityVisitor.put("id", idPerson.toString());
        }


        return identityVisitor;
    }


    //Получение и передача данных для отображения страницы
    private void displayPage(Model model, HttpServletRequest request) {
        List<PersonFullName> clientsBySpecialist = specialistsAndClientService.getClientsBySpecialistList((long) request.getSession().getAttribute("id"));
        List<UnregisteredPerson> unregisteredBySpecialist = unregisteredPersonService.getUnregisteredPersonBySpecialistList((long) request.getSession().getAttribute("id"));
//        Map<LocalDate, Map<String, String>> sortedFreeSchedule = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));

//        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        Map<LocalDate, Map<String, String>> schedule = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));
//        String fullName = assignedToSpecialist.get().getVisitorList().getFullName();

        List<String> allCalendar = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<LocalDate> nearestDates = schedule.keySet().stream()
                .filter(date -> !date.isBefore(now)) // исключаем даты, предшествующие текущей дате
                .sorted(Comparator.comparingLong(date -> ChronoUnit.DAYS.between(now, date))).toList();
        for (LocalDate nearestDate : nearestDates) {
            String[][] calendarForView = datesAppointmentsService.getCalendarForClient("specialist", nearestDate, schedule.get(nearestDate));
            try {
                allCalendar.add(objectMapper.writeValueAsString(calendarForView));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        model.addAttribute("nameClient", assignedToSpecialist.get().getVisitorList().getUsername());

        for (int i = 0; i < allCalendar.size(); i++) {
            model.addAttribute("day" + i, allCalendar.get(i));
        }


        model.addAttribute("clientsBySpecialist", clientsBySpecialist);
        model.addAttribute("unregisteredBySpecialist", unregisteredBySpecialist);
        model.addAttribute("name", request.getSession().getAttribute("name"));
//        model.addAttribute("dates", sortedFreeSchedule);
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

    private LocalDateTime parseInLocalDataTime(Map<String, String> dataTime) {
        String meetingDate = dataTime.get("meetingDate");
        String meetingTime = dataTime.get("meetingTime");
        LocalDate date = LocalDate.parse(meetingDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalTime time = LocalTime.parse(meetingTime, DateTimeFormatter.ofPattern("HH:mm"));
        return date.atTime(time);
    }

    //ToDo сделать форму для сопоставления ЗАРЕГ и НЕ_ЗАРЕГ клиентов
}
