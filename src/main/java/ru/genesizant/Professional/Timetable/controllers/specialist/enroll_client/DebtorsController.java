package ru.genesizant.Professional.Timetable.controllers.specialist.enroll_client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.dto.AgreementAppointmentDTO;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/administration")
public class DebtorsController {

    private final JWTUtil jwtUtil;
    private final SpecialistAppointmentsService specialistAppointmentsService;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ENROLL_VIEW_REDIRECT = "redirect:/administration/proof_clients";

    @Autowired
    public DebtorsController(JWTUtil jwtUtil, SpecialistAppointmentsService specialistAppointmentsService) {
        this.jwtUtil = jwtUtil;
        this.specialistAppointmentsService = specialistAppointmentsService;
    }

    @GetMapping("/proof_clients")
    public String listDebtors(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return "specialist/list_debtors";
    }

    @PostMapping("/agreement")
    public String agreementAppointment(Model model, HttpServletRequest request,
                                       @RequestParam("agreementId") Optional<@NotNull String> agreementId) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        if (agreementId.isPresent() && !agreementId.get().equals("")) {
            specialistAppointmentsService.agreementPrePay(Long.valueOf(agreementId.get()), Boolean.TRUE);
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return ENROLL_VIEW_REDIRECT;
    }

    @PostMapping("/no_agreement")
    public String noAgreementAppointment(Model model, HttpServletRequest request,
                                       @RequestParam("agreementId") Optional<@NotNull String> agreementId) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        if (agreementId.isPresent() && !agreementId.get().equals("")) {
            specialistAppointmentsService.agreementPrePay(Long.valueOf(agreementId.get()), Boolean.FALSE);
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return ENROLL_VIEW_REDIRECT;
    }

    private void displayPage(Model model, HttpServletRequest request) {
        List<SpecialistAppointments> appointmentsList = specialistAppointmentsService.findAllAppointments();
        List<AgreementAppointmentDTO> needPay = new ArrayList<>();
        List<AgreementAppointmentDTO> agreePay = new ArrayList<>();
        if (!appointmentsList.isEmpty()) {
            for (SpecialistAppointments appointments : appointmentsList) {
                AgreementAppointmentDTO appointmentDTO = new AgreementAppointmentDTO();
                appointmentDTO.setIdAppointment(appointments.getId());
                appointmentDTO.setFullName(appointments.getVisitor_appointments().getFullName());
                appointmentDTO.setDateAppointment(appointments.getVisitDate());
                appointmentDTO.setTimeAppointment(appointments.getAppointmentTime());
                if (appointments.isPrepayment()) {
                    agreePay.add(appointmentDTO);
                } else {
                    needPay.add(appointmentDTO);
                }
            }
            model.addAttribute("listName", needPay);
            model.addAttribute("agreePay", agreePay);
        }
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }
}
