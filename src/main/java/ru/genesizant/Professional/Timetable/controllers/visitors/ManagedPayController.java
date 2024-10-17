package ru.genesizant.Professional.Timetable.controllers.visitors;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.model.*;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.*;
import ru.genesizant.Professional.Timetable.services.telegram.SendMessageService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/managed")
public class ManagedPayController {

    private final JWTUtil jwtUtil;
    private final SendMessageService sendMessageService;

    private final SpecialistsAndClientService specialistsAndClientService;
    private final SpecialistPayService specialistPayService;
    private final PersonService personService;
    private final ReceptionService receptionService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        Optional<SpecialistPay> specialistPay = Optional.empty();
        if (assignedToSpecialist.isPresent()) {
            specialistPay = specialistPayService.findBySpecialistPay(assignedToSpecialist.get().getSpecialistList().getId());
        }

        List<Reception> needPay = receptionService.findNeedPayReception(assignedToSpecialist.get());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        needPay.forEach(reception -> {
            reception.setFormattedDate(reception.getDateVacant().format(formatter));
        });
        model.addAttribute("needPay", needPay);

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
        return "visitors/managed_pay";
    }

    @PostMapping("/confirm") // Укажите здесь URL, который соответствует URL в JavaScript запросе
    public String handleTableClick(@ModelAttribute("visitor") Person visitor,
                                   @RequestBody Map<String, String> applicationFromSpecialist) {
        Reception reception = receptionService.findById(Long.valueOf(applicationFromSpecialist.get("id")));
        reception.setPrepaymentVisitor(true);
        receptionService.save(reception);
        sendMessageService.notifyPaymentFromClient(visitor.getFullName(), reception.getSpecIdReception(), reception);
        log.info("Клиент: " + visitor.getFullName() + ". Нажал кнопку подтверждение оплаты на консультацию: " + applicationFromSpecialist.get("id"));
        return "redirect:/managed/managed_pay";
    }
}
