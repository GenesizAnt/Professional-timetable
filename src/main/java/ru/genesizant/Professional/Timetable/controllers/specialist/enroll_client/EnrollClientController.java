package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

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
import java.time.format.DateTimeFormatter;
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
    private final ModelMapper modelMapper;

    private final PersonService personService;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final UnregisteredPersonService unregisteredPersonService;
    private final VacantSeatService vacantSeatService;
    private final ReceptionService receptionService;

    @ModelAttribute
    public void getPayloadPage(@ModelAttribute("specialist") Person specialist, Model model, HttpServletRequest request) {
        List<PersonFullName> clientsBySpecialist = specialistsAndClientService.getClientsBySpecialistList((long) request.getSession().getAttribute("id"));
        List<UnregisteredPerson> unregisteredBySpecialist = unregisteredPersonService.getUnregisteredPersonBySpecialistList((long) request.getSession().getAttribute("id"));
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

        int totalPages = vacantSeatsPage.getTotalPages();
        int currentPage = vacantSeatsPage.getNumber();
        int visiblePages = 5; // Количество отображаемых страниц

        int startPage = Math.max(0, currentPage - visiblePages / 2);
        int endPage = Math.min(totalPages - 1, startPage + visiblePages - 1);

        // Передайте эти значения в модель
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

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

        int totalPagesAp = aproveReceptions.getTotalPages();
        int currentPageAp = aproveReceptions.getNumber();
        int visiblePagesAp = 5; // Количество отображаемых страниц

        int startPageAp = Math.max(0, currentPageAp - visiblePagesAp / 2);
        int endPageAp = Math.min(totalPagesAp - 1, startPageAp + visiblePagesAp - 1);

// Передайте эти значения в модель
        model.addAttribute("startPageAp", startPageAp);
        model.addAttribute("endPageAp", endPageAp);

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

    @PostMapping("/record-client")
    public ResponseEntity<String> handleRecordClient(@RequestBody Map<String, String> applicationFromSpecialist,
                                                     @ModelAttribute("specialist") Person specialist) {
        VacantSeat vacantSeat = vacantSeatService.findById(Long.valueOf(applicationFromSpecialist.get("id")));

        if (applicationFromSpecialist.get("registeredStatus") == null) {
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
}
