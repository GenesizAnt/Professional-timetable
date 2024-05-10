package ru.genesizant.Professional.Timetable.controllers.visitors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegram;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegramService;

@Controller
@RequestMapping("/profile")
public class VisitorProfileController {

    private final JWTUtil jwtUtil;
    private final UserTelegramService userTelegramService;
    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String PROFILE_VIEW_REDIRECT = "redirect:/profile/my_profile";

    public VisitorProfileController(JWTUtil jwtUtil, UserTelegramService userTelegramService, SpecialistAppointmentsService specialistAppointmentsService, SpecialistsAndClientService specialistsAndClientService) {
        this.jwtUtil = jwtUtil;
        this.userTelegramService = userTelegramService;
        this.specialistAppointmentsService = specialistAppointmentsService;
        this.specialistsAndClientService = specialistsAndClientService;
    }

    // Отображение страницы профиля клиента
    @GetMapping("/my_profile")
    public String listDebtors(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return "visitors/visitorprofile";
    }

    // Подтвердить аккаунт в ТГ боте
    @GetMapping("/agreeTG")
    public String agreeTG(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        UserTelegram userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        userTelegram.setAgree(Boolean.TRUE);
        userTelegramService.save(userTelegram);
        return PROFILE_VIEW_REDIRECT;
    }

    // Отметить подтверждение зарегистрированного аккаунта в ТГ боте - т.е. удалить его
    @GetMapping("/cancelTG")
    public String cancelTG(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        userTelegramService.deleteByPersonId((Long) request.getSession().getAttribute("id"));
        return PROFILE_VIEW_REDIRECT;
    }


    private void displayPage(Model model, HttpServletRequest request) {
        UserTelegram userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        if (userTelegram == null) {
            model.addAttribute("notacc", "");
        } else {
            if (userTelegram.isAgree()) {
                model.addAttribute("agree", "подтвержден");
            }
            if (!userTelegram.isAgree()) {
                model.addAttribute("notagree", "не подтвержден");
                model.addAttribute("username", userTelegram.getPersonusername());
            }
        }

        model.addAttribute("name", request.getSession().getAttribute("name"));
    }
}
