package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.AgreementAppointmentDTO;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.Reception;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.ReceptionService;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/administration")
public class DebtorsController {

    private final String ENROLL_VIEW_REDIRECT = "redirect:/administration/proof_clients";

    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final PersonService personService;
    private final ReceptionService receptionService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
//        List<SpecialistAppointments> appointmentsList = specialistAppointmentsService.findAllAppointmentsBySpecialist((long) request.getSession().getAttribute("id"));
        List<Reception> appointmentsList = receptionService.findListDebitors((long) request.getSession().getAttribute("id"));
//        List<AgreementAppointmentDTO> needPay = new ArrayList<>();
//        List<AgreementAppointmentDTO> agreePay = new ArrayList<>();
//        List<AgreementAppointmentDTO> maybePay = new ArrayList<>();
//        if (!appointmentsList.isEmpty()) {
//            for (Reception appointments : appointmentsList) {
//                AgreementAppointmentDTO appointmentDTO = new AgreementAppointmentDTO();
//                appointmentDTO.setIdAppointment(appointments.getId());
//                appointmentDTO.setFullName(appointments.getVisitorAppointments().getFullName());
//                appointmentDTO.setDateAppointment(appointments.getVisitDate());
//                appointmentDTO.setTimeAppointment(appointments.getAppointmentTime());
//                if (appointments.isPrepaymentVisitor() && !appointments.isPrepayment()) {
//                    maybePay.add(appointmentDTO); //клиент подтвердил, спец нет
//                }
//                if (appointments.isPrepayment()) {
//                    agreePay.add(appointmentDTO); // спец подтвердил
//                } else {
//                    needPay.add(appointmentDTO); // ни спец ни клиент не подтвердил оплату
//                }
//            }
//            model.addAttribute("agreePay", agreePay);
//            model.addAttribute("maybePay", maybePay);
//        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        appointmentsList.forEach(reception -> {
            reception.setFormattedDate(reception.getDateVacant().format(formatter));
        });
        model.addAttribute("listName", appointmentsList);
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

    //Кнопка для подтверждения оплаты по конкретному клиенту
    @PostMapping("/agreement")
    public String agreementAppointment(@ModelAttribute("specialist") Person person,
                                       @RequestParam("agreementId") String agreementId) {
        if (!agreementId.isEmpty()) {
            specialistAppointmentsService.agreementPrePay(Long.valueOf(agreementId), Boolean.TRUE);
            log.info("Спец: " + person.getFullName() + ". Подтвердил оплату по консультации с ID:" + agreementId);
        }
        return ENROLL_VIEW_REDIRECT;
    }

    //Кнопка для ОТМЕНЫ подтверждения оплаты по конкретному клиенту
    @PostMapping("/no_agreement")
    public String noAgreementAppointment(@ModelAttribute("specialist") Person person,
                                         @RequestParam("agreementId") String agreementId) {
        if (!agreementId.isEmpty()) {
            specialistAppointmentsService.agreementPrePay(Long.valueOf(agreementId), Boolean.FALSE);
            log.info("Спец: " + person.getFullName() + ". Отменил оплату по консультации с ID:" + agreementId);
        }
        return ENROLL_VIEW_REDIRECT;
    }

    @PostMapping("/confirm") // Укажите здесь URL, который соответствует URL в JavaScript запросе
    public String handleTableClick(@ModelAttribute("specialist") Person person,
                                   @RequestBody Map<String, String> applicationFromSpecialist) {
        specialistAppointmentsService.agreementPrePay(Long.valueOf(applicationFromSpecialist.get("id")), Boolean.TRUE);
        log.info("Спец: " + person.getFullName() + ". Подтвердил оплату через таблицу по консультации с ID:" + applicationFromSpecialist.get("id"));
        return ENROLL_VIEW_REDIRECT;
    }

}
