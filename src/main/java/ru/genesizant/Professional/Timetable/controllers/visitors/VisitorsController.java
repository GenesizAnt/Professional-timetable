package ru.genesizant.Professional.Timetable.controllers.visitors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.model.*;
import ru.genesizant.Professional.Timetable.services.*;
import ru.genesizant.Professional.Timetable.services.telegram.SendMessageService;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static ru.genesizant.Professional.Timetable.enums.StatusPerson.VISITOR;
import static ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor.REGISTERED;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/visitors")
public class VisitorsController {

    private final SendMessageService sendMessageService;
    private final String ENROLL_VIEW_REDIRECT = "redirect:/visitors/my_specialist_menu";

    private final PersonService personService;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final VacantSeatService vacantSeatService;
    private final ReceptionService receptionService;

    @ModelAttribute
    public void getPayloadPage(@ModelAttribute("visitor") Person visitor, Model model, HttpServletRequest request) {
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        model.addAttribute("nameClient", assignedToSpecialist.get().getVisitorList().getUsername());
        model.addAttribute("idSpecialist", assignedToSpecialist.get().getSpecialistList().getId());
        List<Reception> needPay = receptionService.findNeedPayReception(assignedToSpecialist.get());
        model.addAttribute("needPay", needPay);

        // Обработка параметров пагинации
        int page = request.getSession().getAttribute("page") != null ? (int) request.getSession().getAttribute("page") : 0;
        int size = request.getSession().getAttribute("size") != null ? (int) request.getSession().getAttribute("size") : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateVacant").and(Sort.by("timeVacant")));

        Page<VacantSeat> vacantSeatsPage = vacantSeatService.getVacantSeatsPageVisitor(visitor.getId(), pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        vacantSeatsPage.getContent().forEach(vacantSeat -> {
            vacantSeat.setFormattedDate(vacantSeat.getDateVacant().format(formatter));
        });

        model.addAttribute("vacantSeatsVisitor", vacantSeatsPage.getContent());
        model.addAttribute("page", vacantSeatsPage);
    }

    @ModelAttribute(name = "visitor")
    public Person getVisitor(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    //ToDo в разделе Мой профиль - добавить окно с выбором специалистов за которыми закреплен клиент, чтобы клиент мог переключаться между ними (специалистами)

    //Отображение меню специалиста за которым закреплен клиент
    @GetMapping("/my_specialist_menu") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
    public String getMySpecialistMenu(@ModelAttribute("visitor") Person visitor) {
        return "visitors/my_specialist_menu";
    }

    @PostMapping("/record-visitor")
    public String recordVisitorReception(@ModelAttribute("visitor") Person visitor, @RequestBody Map<String, String> applicationFromSpecialist) {
        VacantSeat vacantSeat = vacantSeatService.findById(Long.valueOf(applicationFromSpecialist.get("id")));
        Person specialist = personService.findById(Long.valueOf(applicationFromSpecialist.get("specialistId"))).orElseThrow();
        receptionService.recordNewReception(vacantSeat, visitor, null, specialist, VISITOR, REGISTERED);
        vacantSeat.setIdVisitor(visitor.getId());
        vacantSeat.setFullname(visitor.getFullName());
        vacantSeat.setStatusRegistration(REGISTERED.name());
        vacantSeatService.save(vacantSeat);
        sendMessageService.notifyEnrollNewAppointment(VISITOR, vacantSeat.getDateVacant(), vacantSeat.getTimeVacant(),
                visitor.getId(), specialist.getId());
        log.info("Клиент: " + visitor.getFullName() + ". Записался через кнопку в таблице на: " + vacantSeat.getDateVacant() + vacantSeat.getTimeVacant());
        return ENROLL_VIEW_REDIRECT;
    }

    @PostMapping("/cancel")
    public String cancelEnrollVisitor(@ModelAttribute("visitor") Person visitor, @RequestBody Map<String, String> applicationFromSpecialist) {
        VacantSeat vacantSeat = vacantSeatService.findById(Long.valueOf(applicationFromSpecialist.get("id")));
        Person specialist = personService.findById(Long.valueOf(applicationFromSpecialist.get("specialistId"))).orElseThrow();
        sendMessageService.notifyCancellation(VISITOR, vacantSeat, specialist);
        vacantSeat.setIdVisitor(null);
        vacantSeat.setFullname(null);
        vacantSeat.setStatusRegistration(null);
        log.info("Клиент: " + visitor.getFullName() + ". Отменил запись на: " + vacantSeat.getDateVacant() + vacantSeat.getTimeVacant());
        vacantSeatService.save(vacantSeat);
        receptionService.removeByVacantSeat(vacantSeat, specialist);
        return ENROLL_VIEW_REDIRECT;
    }

    @GetMapping("/vacantSeatsVisitor")
    public String getVacantSeats(Model model, @ModelAttribute("visitor") Person visitor,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        if (page < 0) {
            page = 0; // Устанавливаем на страницу 0, если индекс меньше 0
        }
        request.getSession().setAttribute("page", page);
        request.getSession().setAttribute("size", size);
        Page<VacantSeat> all = vacantSeatService.findAllVacantSeatForVisitor(visitor.getId(), PageRequest.of(page, size));
        model.addAttribute("vacantSeatsVisitor", all.getContent());
        model.addAttribute("page", all);
        model.addAttribute("size", size);
        return ENROLL_VIEW_REDIRECT;
    }
}
