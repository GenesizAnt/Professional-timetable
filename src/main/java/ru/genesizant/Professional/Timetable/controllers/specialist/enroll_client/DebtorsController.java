package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.Reception;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.ReceptionService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/administration")
public class DebtorsController {

    private final String ENROLL_VIEW_REDIRECT = "redirect:/administration/proof_clients";

    private final PersonService personService;
    private final ReceptionService receptionService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        List<Reception> appointmentsList = receptionService.findListDebitors((long) request.getSession().getAttribute("id"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        appointmentsList.forEach(reception -> {
            reception.setFormattedDate(reception.getDateVacant().format(formatter));
        });
        model.addAttribute("listName", appointmentsList);
        List<Reception> agreePay = appointmentsList.stream().filter(reception -> reception.isPrepaymentVisitor() && !reception.isPrepayment()).toList();
        model.addAttribute("agreePay", agreePay);
        model.addAttribute("visitDates", appointmentsList);
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }

    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    //Отображение страницы для подтверждения оплат от клиентов
    @GetMapping("/proof_clients")
    public String listDebtors(@ModelAttribute("specialist") Person person) {
        return "specialist/list_debtors";
    }

    //Кнопка для ОТМЕНЫ подтверждения оплаты по конкретному клиенту
    @PostMapping("/no_agreement")
    public String noAgreementAppointment(@ModelAttribute("specialist") Person person,
                                         @RequestParam("agreementId") String agreementId) {
        if (!agreementId.isEmpty()) {
            Reception reception = receptionService.findById(Long.valueOf(agreementId));
            reception.setPrepaymentVisitor(false);
            receptionService.save(reception);
            log.info("Спец: " + person.getFullName() + ". Отменил оплату по консультации с ID:" + agreementId);
        }
        return ENROLL_VIEW_REDIRECT;
    }

    @PostMapping("/confirm-pay")
    public String handleTableClick(@ModelAttribute("specialist") Person person,
                                   @RequestBody Map<String, String> applicationFromSpecialist) {

        Reception reception = receptionService.findById(Long.parseLong(applicationFromSpecialist.get("id")));
        switch (applicationFromSpecialist.get("status")) {
            case "visitor" -> reception.setPrepaymentVisitor(true);
            case "specialist" -> reception.setPrepayment(true);
        }
        receptionService.save(reception);
        log.info("Спец: " + person.getFullName() + ". Подтвердил оплату через таблицу по консультации с ID:" + applicationFromSpecialist.get("id"));
        return ENROLL_VIEW_REDIRECT;
    }
}
