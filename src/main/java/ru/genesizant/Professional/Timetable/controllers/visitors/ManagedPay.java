package ru.genesizant.Professional.Timetable.controllers.visitors;

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
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/managed")
public class ManagedPay {

    private final JWTUtil jwtUtil;
    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ENROLL_VIEW_REDIRECT = "redirect:/managed/managed_pay";

    @Autowired
    public ManagedPay(JWTUtil jwtUtil, SpecialistAppointmentsService specialistAppointmentsService, SpecialistsAndClientService specialistsAndClientService) {
        this.jwtUtil = jwtUtil;
        this.specialistAppointmentsService = specialistAppointmentsService;
        this.specialistsAndClientService = specialistsAndClientService;
    }

    @GetMapping("/managed_pay")
    public String listDebtors(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return "visitors/managed_pay";
    }

    @PostMapping("/make_payment")
    public String makePayment(Model model, HttpServletRequest request,
                                         @RequestParam("agreementId") Optional<@NotNull String> agreementId) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        if (agreementId.isPresent() && !agreementId.get().equals("")) {
            specialistAppointmentsService.agreementVisitorPrePay(Long.valueOf(agreementId.get()), Boolean.TRUE);
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return ENROLL_VIEW_REDIRECT;
    }

    private void displayPage(Model model, HttpServletRequest request) {
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        List<SpecialistAppointments> appointmentsList = specialistAppointmentsService.findAppointmentsByVisitor((Long) request.getSession().getAttribute("id"),  assignedToSpecialist.get().getSpecialistList().getId());
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
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }
}
