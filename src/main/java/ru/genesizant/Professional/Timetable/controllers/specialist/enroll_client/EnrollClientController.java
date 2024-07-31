package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;
import ru.genesizant.Professional.Timetable.services.*;
import ru.genesizant.Professional.Timetable.services.telegram.SendMessageService;

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

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/enroll")
public class EnrollClientController {

    private final String ENROLL_VIEW_REDIRECT = "redirect:/enroll/enroll_page";
    private final SendMessageService sendMessageService;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final UnregisteredPersonService unregisteredPersonService;
    private final SpecialistAppointmentsService specialistAppointmentsService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        List<PersonFullName> clientsBySpecialist = specialistsAndClientService.getClientsBySpecialistList((long) request.getSession().getAttribute("id"));
        List<UnregisteredPerson> unregisteredBySpecialist = unregisteredPersonService.getUnregisteredPersonBySpecialistList((long) request.getSession().getAttribute("id"));
        List<SpecialistAppointments> appointmentsList = specialistAppointmentsService.findAllAppointmentsBySpecialist((long) request.getSession().getAttribute("id"));
//        List<LocalDateTime> times = new ArrayList<>();
//        if (!appointmentsList.isEmpty()) {
//            for (SpecialistAppointments appointments : appointmentsList) {
//                if (!appointments.isPrepayment()) {
//                    times.add(appointments.getVisitDate().atTime(appointments.getAppointmentTime()));
//                }
//            }
//            //ToDo здесь еще должно быть время посещения
//            //ToDo Перебор по всему списку??
//        }
//        model.addAttribute("visitDates", times);
        Map<LocalDate, Map<String, String>> schedule = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));
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
                log.error("Ошибка формирования JSON из календаря:" + Arrays.deepToString(calendarForView) + ". Текст сообщения - " + e.getMessage());
            }
        }
        for (int i = 0; i < allCalendar.size(); i++) {
            model.addAttribute("day" + i, allCalendar.get(i));
        }
        model.addAttribute("clientsBySpecialist", clientsBySpecialist);
        model.addAttribute("unregisteredBySpecialist", unregisteredBySpecialist);
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }

    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    //Отображение страницы для Записи Клиента
    @GetMapping("/enroll_page")
    public String addAdmissionCalendarUpdate(@ModelAttribute("specialist") Person specialist) { //ToDo showEnrollPage
        return "specialist/enroll_client_view";
    }

    //Выбор и отображение Клиента для записи
    @PostMapping("/customerForRecording")
    public String customerForRecording(@ModelAttribute("specialist") Person specialist, Model model, //ToDo chooseCustomerForRecording
                                       @RequestParam("clientFullName") String clientId,
                                       @RequestParam("registeredStatus") StatusRegisteredVisitor registeredStatus) {
        if (!clientId.isEmpty()) {
            handleCustomerSelection(model, clientId, registeredStatus);
            log.info("Спец: " + specialist.getFullName() + ". Выбрал клиента для записи:" + clientId);
        } else {
            return encodeError("Для работы с клиентом нужно сначала его выбрать!");
        }
        return "specialist/enroll_client_view";
    }

    //Специалист создает незарегистрированного пользователя
    @PostMapping("/newUnregisteredPerson")
    public String newUnregisteredPerson(@ModelAttribute("specialist") Person specialist, //ToDo addNewUnregisteredPerson
                                        @RequestParam("usernameUnregistered") String username,
                                        @RequestParam("surnameUnregistered") String surname,
                                        @RequestParam("patronymicUnregistered") String patronymic) {
        if (isValidPersonInformation(username, surname, patronymic)) {
            unregisteredPersonService.addNewUnregisteredPerson(username, surname, patronymic, personService.findById(specialist.getId()));
            log.info("Спец: " + specialist.getFullName() + ". создает незарегистрированного пользователя");
        } else {
            return encodeError("Чтобы создать незарегистрированного в приложении клиента нужно указать ФИО полностью");
        }
        return ENROLL_VIEW_REDIRECT;
    }

    //Запись клиента на выбранную дату по кнопке
    @PostMapping("/newDatesAppointments")
    public String newDatesAppointments(@ModelAttribute("specialist") Person specialist,
                                       @RequestParam("meeting") LocalDateTime meeting,
                                       @RequestParam("selectedCustomerId") String selectedCustomerId,
                                       @RequestParam("registeredStatus") StatusRegisteredVisitor registeredStatus) {
        if (isValidMeetingRequestParameters(meeting, selectedCustomerId, registeredStatus) && !specialistAppointmentsService.isAppointmentExist(specialist.getId(), meeting)) {
            //ToDo сделать Валид для проверки что время не забронированно
            PersonFullName visitorFullName = null;
            Person visitor = null;
            if (registeredStatus.equals(REGISTERED)) {
                 visitor = personService.findById(Long.valueOf(selectedCustomerId)).get();
                //ToDo упростить метод ужас как много передается в аргументах
                specialistAppointmentsService.createNewAppointments(
                        meeting.toLocalDate(),
                        meeting.toLocalTime(),
                        specialist,
                        visitor,
                        Boolean.FALSE, Boolean.FALSE);
                sendMessageService.notifyEnrollNewAppointment(SPECIALIST, meeting, visitor.getId(), specialist.getId());
            } else if (registeredStatus.equals(UNREGISTERED)) {
                visitorFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
            }

            if (visitorFullName == null) {
                visitorFullName = modelMapper.map(visitor, PersonFullName.class);
            }
            datesAppointmentsService.enrollVisitorNewAppointments(meeting, visitorFullName, specialist.getId(), SPECIALIST);
            log.info("Спец: " + specialist.getFullName() + ". Записал клиента: " + selectedCustomerId + " на " + meeting);
        } else {
            return encodeError("Для записи нужно выбрать клиента, время и дату записи. Либо время уже занято");
        }
        return ENROLL_VIEW_REDIRECT;
    }

    //Запись клиента на выбранную дату из таблицы
    @PostMapping("/newDatesAppointmentsTable")
    public String newDatesAppointmentsTable(@ModelAttribute("specialist") Person specialist,
                                            @RequestBody Map<String, String> applicationFromSpecialist) {
        String selectedCustomerId = applicationFromSpecialist.get("selectedCustomerId");
        String registeredStatus = applicationFromSpecialist.get("registeredStatus");
        LocalDateTime meetingDateTime = parseInLocalDataTime(applicationFromSpecialist);

        if (selectedCustomerId != null && registeredStatus != null &&
                !selectedCustomerId.isEmpty() && !registeredStatus.isEmpty()) { //ToDo сделать другую проверку

            //ToDo насколько дублируется код из предыдущ метода?
            if (registeredStatus.equals(REGISTERED.name())) {
                PersonFullName personFullName = modelMapper.map(personService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, specialist.getId(), SPECIALIST);
                specialistAppointmentsService.createNewAppointments(meetingDateTime.toLocalDate(), meetingDateTime.toLocalTime(),
                        personService.findById(specialist.getId()).get(),
                        personService.findById(personFullName.getId()).get(), Boolean.FALSE, Boolean.FALSE);

                sendMessageService.notifyEnrollNewAppointment(SPECIALIST, meetingDateTime, personFullName.getId(), specialist.getId());
            } else if (registeredStatus.equals(UNREGISTERED.name())) {
                PersonFullName personFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
                datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, specialist.getId(), SPECIALIST);
            }
            log.info("Спец: " + specialist.getFullName() + ". Запись клиента: " + selectedCustomerId + " на выбранную дату из таблицы: " + meetingDateTime);
        } else {
            return encodeError("Для записи нужно выбрать клиента, время и дату записи");
        }

        return ENROLL_VIEW_REDIRECT;
    }

    //Специалист отменяет запись ранее записанного клиента
    @PostMapping("/cancellingBooking")
    public String cancellingBooking(@ModelAttribute("specialist") Person specialist,
                                    @RequestParam("meetingCancel") LocalDateTime meetingCancel) {
        if (meetingCancel != null) {
            sendMessageService.notifyCancellation(SPECIALIST, meetingCancel, specialist.getId());
            datesAppointmentsService.cancellingBookingAppointments(meetingCancel, specialist.getId());
            specialistAppointmentsService.removeAppointment(meetingCancel, specialist.getId());
            log.info("Спец: " + specialist.getFullName() + ". Отменяет запись на: " + meetingCancel);
        } else {
            return encodeError("Для отмены записи нужно выбрать КЛИЕНТА и ДАТУ отмены приема");
        }
        return ENROLL_VIEW_REDIRECT;
    }


    // Подтверждение записи встречи, которую инициировал клиент
    @PostMapping("/recordingConfirmed")
    public String recordingConfirmed(@ModelAttribute("specialist") Person specialist,
                                     HttpServletRequest request, @RequestBody Map<String, String> recordingIsConfirmed) {
        PersonFullName personFullName = getPersonFullName(recordingIsConfirmed.get("meetingPerson"));
        Map<String, String> identificationMeetingPerson = getIdentificationMeetingPerson(personFullName);
        LocalDateTime meetingDateTime = parseInLocalDataTime(recordingIsConfirmed);

        datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, specialist.getId(), SPECIALIST);

        //ToDo вынести метод в другое место? контролер решает две задачи
        //ToDo ужас твориться с таблицей надо перепроверить все поля, например дата и продолжительность
        //ToDo переименовать поле appointmenttime в Сикуль - нижнее подчеркивание
        if (!identificationMeetingPerson.isEmpty()) {
            specialistAppointmentsService.createNewAppointments(meetingDateTime.toLocalDate(), meetingDateTime.toLocalTime(),
                    personService.findById(specialist.getId()).get(),
                    personService.findById(Long.valueOf(identificationMeetingPerson.get("id"))).get(), Boolean.FALSE, Boolean.FALSE);
            sendMessageService.notifyEnrollNewAppointment(SPECIALIST, meetingDateTime, Long.valueOf(identificationMeetingPerson.get("id")), specialist.getId());
            log.info("Спец: " + specialist.getFullName() + ". Подтвердил запись встречи, которую инициировал клиент: " + personFullName + " на выбранную дату из таблицы: " + meetingDateTime);
        } else {
            log.error("Спец: " + request.getSession().getAttribute("id") + ". Не удалось найти клиента с таким ФИО, свяжитесь с администратором приложения" + personFullName);
            return encodeError("Не удалось найти клиента с таким ФИО, свяжитесь с администратором приложения");
        }
        return ENROLL_VIEW_REDIRECT;
    }

    // Получить ФИО из запроса, в таблице ФИО хранится одной строкой
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

    // ОПАСНЫЙ МЕТОД ищет ИД специалиста по ФИО - опасно т.к. может быть тезка
    private Map<String, String> getIdentificationMeetingPerson(PersonFullName fioPerson) {
        Map<String, String> identityVisitor = new HashMap<>();
        Optional<Person> person = personService.findByFullName(
                fioPerson.getUsername(),
                fioPerson.getSurname(),
                fioPerson.getPatronymic());
        if (person.isPresent()) {
            Optional<SpecialistsAndClient> specialistsAndClient = specialistsAndClientService.findByVisitorListId(person.get().getId());
            if (specialistsAndClient.isPresent()) {
                identityVisitor.put("id", person.get().getId().toString());
                identityVisitor.put("name", fioPerson.getUsername());
                identityVisitor.put("surname", fioPerson.getSurname());
                identityVisitor.put("patronymic", fioPerson.getPatronymic());
            }
        }
        return identityVisitor;
    }

    //Получение клиента для отображения на странице
    private void handleCustomerSelection(Model model, String clientId, StatusRegisteredVisitor registeredStatus) {
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
    }

    private String encodeError(String error) {
        String ERROR_VALIDATE_FORM = "redirect:/enroll/enroll_page?error=";
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }

    private boolean isValidPersonInformation(String username, String surname, String patronymic) {
        return !username.isEmpty() && !surname.isEmpty() && !patronymic.isEmpty();
    }

    private boolean isValidMeetingRequestParameters(LocalDateTime meeting, String selectedCustomerId, StatusRegisteredVisitor registeredStatus) {
        return meeting != null && !selectedCustomerId.isEmpty() && registeredStatus != null;
    }

    // Получить LocalDateTime из Мар
    private LocalDateTime parseInLocalDataTime(Map<String, String> dataTime) {
        String meetingDate = dataTime.get("meetingDate");
        String meetingTime = dataTime.get("meetingTime");
        LocalDate date = LocalDate.parse(meetingDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalTime time = LocalTime.parse(meetingTime, DateTimeFormatter.ofPattern("HH:mm"));
        return date.atTime(time);
    }
}
