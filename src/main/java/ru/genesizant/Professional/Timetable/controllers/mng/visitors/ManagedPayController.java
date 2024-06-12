package ru.genesizant.Professional.Timetable.controllers.mng.visitors;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.AgreementAppointmentDTO;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.model.SpecialistPay;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;
import ru.genesizant.Professional.Timetable.services.SpecialistPayService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/managed")
public class ManagedPayController {

    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final JWTUtil jwtUtil;

    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final SpecialistPayService specialistPayService;
    private final PersonService personService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        List<SpecialistAppointments> appointmentsList = List.of();
        Optional<SpecialistPay> specialistPay = Optional.empty();
        if (assignedToSpecialist.isPresent()) {
            appointmentsList = specialistAppointmentsService.findAppointmentsByVisitor((Long) request.getSession().getAttribute("id"), assignedToSpecialist.get().getSpecialistList().getId());
            specialistPay = specialistPayService.findBySpecialistPay(assignedToSpecialist.get().getSpecialistList().getId());
        }
        List<AgreementAppointmentDTO> needPay = new ArrayList<>();
        if (!appointmentsList.isEmpty()) {
            for (SpecialistAppointments appointments : appointmentsList) {
                AgreementAppointmentDTO appointmentDTO = new AgreementAppointmentDTO();
                appointmentDTO.setIdAppointment(appointments.getId());
                appointmentDTO.setDateAppointment(appointments.getVisitDate());
                appointmentDTO.setTimeAppointment(appointments.getAppointmentTime());
                if (!appointments.isPrepaymentVisitor()) {
                    needPay.add(appointmentDTO);
                }
            }
            model.addAttribute("needPay", needPay);
        }
        specialistPay.ifPresent(pay -> model.addAttribute("link", pay.getLinkPay()));
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }

    @ModelAttribute(name = "visitor")
    public Person getVisitor(HttpServletRequest request) {
        if (jwtUtil.isValidJWTInRun(request)) {
            return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
        } else {
            log.error("Ошибка валидации JWT токена у пользователя - " + request.getSession().getAttribute("id"));
            throw new JWTVerificationException("");
        }
    }

    // Отображение страницы управление оплатами
    @GetMapping("/managed_pay")
    public String listDebtorsV(@ModelAttribute("visitor") Person visitor) {
//        if (jwtUtil.isValidJWTAndSession(request)) {
//            displayPage(model, request);
//            log.info("Клиент: " + visitor.getFullName() + ". Перешел  на страницу управления оплатами");
//        } else {
//            return ERROR_LOGIN;
//        }
        return "visitors/managed_pay";
    }

    // Кнопка Подтверждение оплаты
    @PostMapping("/make_payment")
    public String makePayment(@ModelAttribute("visitor") Person visitor, HttpServletRequest request,
                                         @RequestParam("agreementId") String agreementId) {
//        if (!jwtUtil.isValidJWTAndSession(request)) {
//            return ERROR_LOGIN;
//        }
        if (!agreementId.isEmpty()) {
            specialistAppointmentsService.agreementVisitorPrePay(Long.valueOf(agreementId), Boolean.TRUE);
//            displayPage(model, request);
            log.info("Клиент: " + visitor.getFullName() + ". Нажал кнопку подтверждение оплаты на консультацию: " + agreementId);
        } else {
            return ERROR_LOGIN;
        }
        return "redirect:/managed/managed_pay";
    }

//    private void displayPage(Model model, HttpServletRequest request) {
//        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
//        List<SpecialistAppointments> appointmentsList = List.of();
//        Optional<SpecialistPay> specialistPay = Optional.empty();
//        if (assignedToSpecialist.isPresent()) {
//            appointmentsList = specialistAppointmentsService.findAppointmentsByVisitor((Long) request.getSession().getAttribute("id"), assignedToSpecialist.get().getSpecialistList().getId());
//            specialistPay = specialistPayService.findBySpecialistPay(assignedToSpecialist.get().getSpecialistList().getId());
//        }
//        List<AgreementAppointmentDTO> needPay = new ArrayList<>();
//        if (!appointmentsList.isEmpty()) {
//            for (SpecialistAppointments appointments : appointmentsList) {
//                AgreementAppointmentDTO appointmentDTO = new AgreementAppointmentDTO();
//                appointmentDTO.setIdAppointment(appointments.getId());
//                appointmentDTO.setDateAppointment(appointments.getVisitDate());
//                appointmentDTO.setTimeAppointment(appointments.getAppointmentTime());
//                if (!appointments.isPrepaymentVisitor()) {
//                    needPay.add(appointmentDTO);
//                }
//            }
//            model.addAttribute("needPay", needPay);
//        }
//        specialistPay.ifPresent(pay -> model.addAttribute("link", pay.getLinkPay()));
//        model.addAttribute("name", request.getSession().getAttribute("name"));
//    }
}
