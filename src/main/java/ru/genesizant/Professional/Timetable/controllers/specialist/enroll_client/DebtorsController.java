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
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.repositories.SpecialistAppointmentsRepository;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/administration")
public class DebtorsController {

    private final JWTUtil jwtUtil;
//    private final PersonService personService;
//    private final DatesAppointmentsService datesAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
//    private final ModelMapper modelMapper;
//    private final UnregisteredPersonService unregisteredPersonService;
    private final SpecialistAppointmentsRepository specialistAppointmentsRepository;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ERROR_VALIDATE_FORM = "redirect:/enroll/enroll_page?error=";
    private final String ENROLL_VIEW_REDIRECT = "redirect:/enroll/enroll_page";

    @Autowired
    public DebtorsController(JWTUtil jwtUtil, SpecialistsAndClientService specialistsAndClientService, SpecialistAppointmentsRepository specialistAppointmentsRepository) {
        this.jwtUtil = jwtUtil;
        this.specialistsAndClientService = specialistsAndClientService;
        this.specialistAppointmentsRepository = specialistAppointmentsRepository;
    }

    @GetMapping("/proof_clients")
    public String listDebtors(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {

            displayPage(model, request);

            List<SpecialistAppointments> appointmentsList = specialistAppointmentsRepository.findAll();
            List<SpecialistAppointments> dept = new ArrayList<>();
            List<Person> deptName = new ArrayList<>();
            if (!appointmentsList.isEmpty()) {
                for (SpecialistAppointments appointments : appointmentsList) {
                    if (!appointments.isPrepayment()) {
                        dept.add(appointments);
                        deptName.add(appointments.getVisitor_appointments());
                    }
                }
            model.addAttribute("appointmentsList", dept);
            model.addAttribute("listName", deptName);
            }

        } else {
            return ERROR_LOGIN;
        }

        return "specialist/list_debtors";
    }

    @PostMapping("/agreement")
    public String agreementAppointment(Model model, HttpServletRequest request,
                                       @RequestParam("clientId") Optional<@NotNull String> clientId) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (clientId.isPresent() && !clientId.get().equals("")) {

//            handleCustomerSelection(model, clientId, registeredStatus);
            displayPage(model, request);

        } else {
//            return encodeError("Для работы с клиентом нужно сначала его выбрать!");
        }
        return "specialist/list_debtors";
    }

    private void displayPage(Model model, HttpServletRequest request) {
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }
}
