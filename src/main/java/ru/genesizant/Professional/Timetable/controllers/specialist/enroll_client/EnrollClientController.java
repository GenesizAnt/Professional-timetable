package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor;
import ru.genesizant.Professional.Timetable.model.*;
import ru.genesizant.Professional.Timetable.services.*;
import ru.genesizant.Professional.Timetable.services.telegram.SendMessageService;

import java.net.URI;
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
    private final VacantSeatService vacantSeatService;
    private final ReceptionService receptionService;

    @ModelAttribute
    public void getPayloadPage(@ModelAttribute("specialist") Person specialist, Model model, HttpServletRequest request) {
        List<PersonFullName> clientsBySpecialist = specialistsAndClientService.getClientsBySpecialistList((long) request.getSession().getAttribute("id"));
        List<UnregisteredPerson> unregisteredBySpecialist = unregisteredPersonService.getUnregisteredPersonBySpecialistList((long) request.getSession().getAttribute("id"));
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


        // Обработка параметров пагинации
        int page = request.getSession().getAttribute("page") != null ? (int) request.getSession().getAttribute("page") : 0;
        int size = request.getSession().getAttribute("size") != null ? (int) request.getSession().getAttribute("size") : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateVacant").and(Sort.by("timeVacant")));

        Page<VacantSeat> vacantSeatsPage = vacantSeatService.getVacantSeatsPage(specialist, pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        vacantSeatsPage.getContent().forEach(vacantSeat -> {
            vacantSeat.setFormattedDate(vacantSeat.getDateVacant().format(formatter));
        });

        model.addAttribute("vacantSeats", vacantSeatsPage.getContent());
        model.addAttribute("page", vacantSeatsPage);

        // Обработка параметров пагинации
        int pageAp = request.getSession().getAttribute("pageAp") != null ? (int) request.getSession().getAttribute("pageAp") : 0;
        int sizeAp = request.getSession().getAttribute("sizeAp") != null ? (int) request.getSession().getAttribute("sizeAp") : 10;
        Pageable pageableAp = PageRequest.of(pageAp, sizeAp, Sort.by("dateVacant").and(Sort.by("timeVacant")));

        Page<Reception> aproveReceptions = receptionService.getReceptionsPage(specialist, pageableAp);
        DateTimeFormatter formatterReceptions = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        aproveReceptions.getContent().forEach(reception -> {
            reception.setFormattedDate(reception.getDateVacant().format(formatterReceptions));
        });

        model.addAttribute("aproveReceptions", aproveReceptions.getContent());
        model.addAttribute("pageAp", aproveReceptions);
    }

    //ToDo Проверить связь при отмене и создании записей на примем и слота времени

    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    //Отображение страницы для Записи Клиента
    @GetMapping("/enroll_page")
    public String addAdmissionCalendarUpdate(@ModelAttribute("specialist") Person specialist) { //ToDo showEnrollPage
        return "specialist/enroll_client_view";
    }

    //Специалист отменяет запись ранее записанного клиента
    @PostMapping("/cancel")
    public String cancelEnrollment(@RequestBody Map<String, String> applicationFromSpecialist, @ModelAttribute("specialist") Person specialist) {
        VacantSeat vacantSeat = vacantSeatService.findById(Long.valueOf(applicationFromSpecialist.get("id")));
        sendMessageService.notifyCancellation(SPECIALIST, vacantSeat, specialist);
        vacantSeat.setIdVisitor(null);
        vacantSeat.setFullname(null);
        vacantSeat.setStatusRegistration(null);
        vacantSeatService.save(vacantSeat);
        receptionService.removeByVacantSeat(vacantSeat, specialist);
        return ENROLL_VIEW_REDIRECT;
    }

    // Подтверждение записи встречи, которую инициировал клиент
    //ToDo НУЖЕН ЛИ ЭТОТ КОНТРОООООООООООООООООООООООООООООООООООООООООООООООООООООООЛЕР
    @PostMapping("/agree-spec")
    public String agreeReceptionSpec(@RequestBody Map<String, String> applicationFromSpecialist) {
        Reception reception = receptionService.findById(Long.valueOf(applicationFromSpecialist.get("id")));
        reception.setConfirmedSpecialist(true);
        receptionService.save(reception);
        sendMessageService.notifyEnrollNewAppointment(SPECIALIST, reception.getDateVacant(), reception.getTimeVacant(),
                reception.getVisitorIdReception().getId(), reception.getSpecIdReception().getId());
        return ENROLL_VIEW_REDIRECT;
    }

    @PostMapping("/agree-visitor")
    public String agreeReceptionVisitor(@RequestBody Map<String, String> applicationFromSpecialist) {
        Reception reception = receptionService.findById(Long.valueOf(applicationFromSpecialist.get("id")));
        reception.setConfirmedVisitor(true);
        receptionService.save(reception);
        return ENROLL_VIEW_REDIRECT;
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

    //Запись клиента на выбранную дату по кнопке - кнопка удалена
//    @PostMapping("/newDatesAppointments")
//    public String newDatesAppointments(@ModelAttribute("specialist") Person specialist,
//                                       @RequestParam("meeting") LocalDateTime meeting,
//                                       @RequestParam("selectedCustomerId") String selectedCustomerId,
//                                       @RequestParam("registeredStatus") StatusRegisteredVisitor registeredStatus) {
//        if (isValidMeetingRequestParameters(meeting, selectedCustomerId, registeredStatus) && !specialistAppointmentsService.isAppointmentExist(specialist.getId(), meeting)) {
//            //ToDo сделать Валид для проверки что время не забронированно
//            PersonFullName visitorFullName = null;
//            Person visitor = null;
//            if (registeredStatus.equals(REGISTERED)) {
//                visitor = personService.findById(Long.valueOf(selectedCustomerId)).get();
//                //ToDo упростить метод ужас как много передается в аргументах
//                specialistAppointmentsService.createNewAppointments(
//                        meeting.toLocalDate(),
//                        meeting.toLocalTime(),
//                        specialist,
//                        visitor,
//                        Boolean.FALSE, Boolean.FALSE);
////                sendMessageService.notifyEnrollNewAppointment(SPECIALIST, meeting, visitor.getId(), specialist.getId());
//            } else if (registeredStatus.equals(UNREGISTERED)) {
//                visitorFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
//            }
//
//            if (visitorFullName == null) {
//                visitorFullName = modelMapper.map(visitor, PersonFullName.class);
//            }
//            datesAppointmentsService.enrollVisitorNewAppointments(meeting, visitorFullName, specialist.getId(), SPECIALIST);
//            log.info("Спец: " + specialist.getFullName() + ". Записал клиента: " + selectedCustomerId + " на " + meeting);
//        } else {
//            return encodeError("Для записи нужно выбрать клиента, время и дату записи. Либо время уже занято");
//        }
//        return ENROLL_VIEW_REDIRECT;
//    }

    //Запись клиента на выбранную дату из таблицы
//    @PostMapping("/newDatesAppointmentsTable")
//    public String newDatesAppointmentsTable(@ModelAttribute("specialist") Person specialist,
//                                            @RequestBody Map<String, String> applicationFromSpecialist) {
//        String selectedCustomerId = applicationFromSpecialist.get("selectedCustomerId");
//        String registeredStatus = applicationFromSpecialist.get("registeredStatus");
//        LocalDateTime meetingDateTime = parseInLocalDataTime(applicationFromSpecialist);
//
//        if (selectedCustomerId != null && registeredStatus != null &&
//                !selectedCustomerId.isEmpty() && !registeredStatus.isEmpty()) { //ToDo сделать другую проверку
//
//            //ToDo насколько дублируется код из предыдущ метода?
//            if (registeredStatus.equals(REGISTERED.name())) {
//                PersonFullName personFullName = modelMapper.map(personService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
//                datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, specialist.getId(), SPECIALIST);
//                specialistAppointmentsService.createNewAppointments(meetingDateTime.toLocalDate(), meetingDateTime.toLocalTime(),
//                        personService.findById(specialist.getId()).get(),
//                        personService.findById(personFullName.getId()).get(), Boolean.FALSE, Boolean.FALSE);
//
//                sendMessageService.notifyEnrollNewAppointment(SPECIALIST, meetingDateTime, personFullName.getId(), specialist.getId());
//            } else if (registeredStatus.equals(UNREGISTERED.name())) {
//                PersonFullName personFullName = modelMapper.map(unregisteredPersonService.findById(Long.valueOf(selectedCustomerId)), PersonFullName.class);
//                datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, specialist.getId(), SPECIALIST);
//            }
//            log.info("Спец: " + specialist.getFullName() + ". Запись клиента: " + selectedCustomerId + " на выбранную дату из таблицы: " + meetingDateTime);
//        } else {
//            return encodeError("Для записи нужно выбрать клиента, время и дату записи");
//        }
//
//        return ENROLL_VIEW_REDIRECT;
//    }

    @PostMapping("/record-client")
    public ResponseEntity<String> handleRecordClient(@RequestBody Map<String, String> applicationFromSpecialist,
                                                     @ModelAttribute("specialist") Person specialist) {
        VacantSeat vacantSeat = vacantSeatService.findById(Long.valueOf(applicationFromSpecialist.get("id")));

        if (applicationFromSpecialist.get("registeredStatus") == null) {
//            return encodeError("Для записи нужно выбрать клиента, время и дату записи. Либо время уже занято");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Для записи нужно выбрать клиента, время и дату записи. Либо время уже занято\"}");
        } else if (applicationFromSpecialist.get("registeredStatus").equals(REGISTERED.name())) {

            Person client = personService.findById(Long.valueOf(applicationFromSpecialist.get("selectedCustomerId"))).orElseThrow();
            receptionService.recordNewReception(vacantSeat, client, null, specialist, SPECIALIST, REGISTERED);
            vacantSeat.setIdVisitor(client.getId());
            vacantSeat.setFullname(client.getFullName());
            vacantSeat.setStatusRegistration(REGISTERED.name());
            vacantSeatService.save(vacantSeat);
            sendMessageService.notifyEnrollNewAppointment(SPECIALIST, vacantSeat.getDateVacant(), vacantSeat.getTimeVacant(), client.getId(), specialist.getId());
        } else if (applicationFromSpecialist.get("registeredStatus").equals(UNREGISTERED.name())) {
            UnregisteredPerson unregisteredPerson = unregisteredPersonService.findById(Long.valueOf(applicationFromSpecialist.get("selectedCustomerId"))).orElseThrow();
            vacantSeat.setIdVisitor(unregisteredPerson.getId());
            vacantSeat.setFullname(unregisteredPerson.getFullName());
            vacantSeat.setStatusRegistration(UNREGISTERED.name());
            vacantSeatService.save(vacantSeat);
            receptionService.recordNewReception(vacantSeat, null, unregisteredPerson, specialist, SPECIALIST, UNREGISTERED);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/enroll/enroll_page"));
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

//    id: id,
//    selectedCustomerId: selectedCustomerId1,
//    registeredStatus: registeredStatus1

    //Специалист отменяет запись ранее записанного клиента
//    @PostMapping("/cancellingBooking")
//    public String cancellingBooking(@ModelAttribute("specialist") Person specialist,
//                                    @RequestParam("meetingCancel") LocalDateTime meetingCancel) {
//        if (meetingCancel != null) {
//            sendMessageService.notifyCancellation(SPECIALIST, meetingCancel, specialist.getId());
//            datesAppointmentsService.cancellingBookingAppointments(meetingCancel, specialist.getId());
//            specialistAppointmentsService.removeAppointment(meetingCancel, specialist.getId());
//            log.info("Спец: " + specialist.getFullName() + ". Отменяет запись на: " + meetingCancel);
//        } else {
//            return encodeError("Для отмены записи нужно выбрать КЛИЕНТА и ДАТУ отмены приема");
//        }
//        return ENROLL_VIEW_REDIRECT;
//    }


//    // Подтверждение записи встречи, которую инициировал клиент
//    @PostMapping("/recordingConfirmed")
//    public String recordingConfirmed(@ModelAttribute("specialist") Person specialist,
//                                     HttpServletRequest request, @RequestBody Map<String, String> recordingIsConfirmed) {
//        PersonFullName personFullName = getPersonFullName(recordingIsConfirmed.get("meetingPerson"));
//        Map<String, String> identificationMeetingPerson = getIdentificationMeetingPerson(personFullName);
//        LocalDateTime meetingDateTime = parseInLocalDataTime(recordingIsConfirmed);
//
//        datesAppointmentsService.enrollVisitorNewAppointments(meetingDateTime, personFullName, specialist.getId(), SPECIALIST);
//
//        //ToDo вынести метод в другое место? контролер решает две задачи
//        //ToDo ужас твориться с таблицей надо перепроверить все поля, например дата и продолжительность
//        //ToDo переименовать поле appointmenttime в Сикуль - нижнее подчеркивание
//        if (!identificationMeetingPerson.isEmpty()) {
//            specialistAppointmentsService.createNewAppointments(meetingDateTime.toLocalDate(), meetingDateTime.toLocalTime(),
//                    personService.findById(specialist.getId()).get(),
//                    personService.findById(Long.valueOf(identificationMeetingPerson.get("id"))).get(), Boolean.FALSE, Boolean.FALSE);
////            sendMessageService.notifyEnrollNewAppointment(SPECIALIST, meetingDateTime, Long.valueOf(identificationMeetingPerson.get("id")), specialist.getId());
//            log.info("Спец: " + specialist.getFullName() + ". Подтвердил запись встречи, которую инициировал клиент: " + personFullName + " на выбранную дату из таблицы: " + meetingDateTime);
//        } else {
//            log.error("Спец: " + request.getSession().getAttribute("id") + ". Не удалось найти клиента с таким ФИО, свяжитесь с администратором приложения" + personFullName);
//            return encodeError("Не удалось найти клиента с таким ФИО, свяжитесь с администратором приложения");
//        }
//        return ENROLL_VIEW_REDIRECT;
//    }

    @GetMapping("/vacantSeats")
    public String getVacantSeats(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        if (page < 0) {
            page = 0; // Устанавливаем на страницу 0, если индекс меньше 0
        }
        request.getSession().setAttribute("page", page);
        request.getSession().setAttribute("size", size);
        Page<VacantSeat> all = vacantSeatService.findAll(PageRequest.of(page, size));
        model.addAttribute("vacantSeats", all.getContent());
        model.addAttribute("page", all);
//        model.addAttribute("size", size);
        return ENROLL_VIEW_REDIRECT;
    }

    @GetMapping("/aproveReceptions")
    public String getAproveReceptions(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        if (page < 0) {
            page = 0; // Устанавливаем на страницу 0, если индекс меньше 0
        }
        request.getSession().setAttribute("pageAp", page);
        request.getSession().setAttribute("sizeAp", size);
        Page<Reception> all = receptionService.findAll(PageRequest.of(page, size));
        model.addAttribute("aproveReceptions", all.getContent());
        model.addAttribute("pageAp", all);
        return ENROLL_VIEW_REDIRECT;
    }

    // Получить ФИО из запроса, в таблице ФИО хранится одной строкой
//    private PersonFullName getPersonFullName(String meetingPerson) {
//        PersonFullName personFullName = new PersonFullName();
//        String[] fioArray = meetingPerson.split(" ");
//        if (fioArray.length == 3) {
//            personFullName.setSurname(fioArray[0]);
//            personFullName.setUsername(fioArray[1]);
//            personFullName.setPatronymic(fioArray[2]);
//        }
//        return personFullName;
//    }

    // ОПАСНЫЙ МЕТОД ищет ИД специалиста по ФИО - опасно т.к. может быть тезка
//    private Map<String, String> getIdentificationMeetingPerson(PersonFullName fioPerson) {
//        Map<String, String> identityVisitor = new HashMap<>();
//        Optional<Person> person = personService.findByFullName(
//                fioPerson.getUsername(),
//                fioPerson.getSurname(),
//                fioPerson.getPatronymic());
//        if (person.isPresent()) {
//            Optional<SpecialistsAndClient> specialistsAndClient = specialistsAndClientService.findByVisitorListId(person.get().getId());
//            if (specialistsAndClient.isPresent()) {
//                identityVisitor.put("id", person.get().getId().toString());
//                identityVisitor.put("name", fioPerson.getUsername());
//                identityVisitor.put("surname", fioPerson.getSurname());
//                identityVisitor.put("patronymic", fioPerson.getPatronymic());
//            }
//        }
//        return identityVisitor;
//    }

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
